/*!
 * Clipboard AutoSuggest – Rust edition
 *
 * Theo dõi clipboard: khi bạn copy 1 từ hoặc cụm 2 từ hợp lệ,
 * tự động thay bằng cụm gợi ý dựa trên TuVung.txt.
 *
 * Chế độ:
 *   • Copy "!"  → TOP TIER: chỉ chọn ngẫu nhiên trong nhóm tốt nhất
 *   • Copy "%"  → SMART RANDOM: 75% top tier / 25% ngẫu nhiên
 *
 * Build (ẩn console):
 *   cargo build --release
 *   (thêm #![windows_subsystem = "windows"] để ẩn console hoàn toàn)
 */

#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

// Nhúng raw RGBA 64×64 vào binary lúc compile — không cần decode PNG lúc runtime
const ICON_RGBA: &[u8] = include_bytes!("../assets/icon_64.rgba");
const ICON_SIZE: u32 = 64;

use std::{
    collections::HashMap,
    fs,
    path::PathBuf,
    sync::{Arc, Mutex},
    thread,
    time::Duration,
};

use unicode_normalization::UnicodeNormalization;
use arboard::Clipboard;
use eframe::egui;
use rand::seq::IndexedRandom;

// ─────────────────────────────────────────────
//  CẤU HÌNH
// ─────────────────────────────────────────────

const POLL_MS: u64 = 280;
const TOP_N: usize = 15;
const CHANCE_RANDOM_ANY: f64 = 0.25; // 25% chọn hoàn toàn ngẫu nhiên (smart mode)

// ─────────────────────────────────────────────
//  INDEX từ TuVung.txt
// ─────────────────────────────────────────────

/// Một entry trong từ điển
#[derive(Clone)]
struct Phrase {
    text: String,  // toàn bộ cụm, VD "an toàn"
    last: String,  // từ cuối,   VD "toàn"
}

/// Cấu trúc index chính
struct PhraseIndex {
    /// last_word → danh sách phrase bắt đầu bằng last_word (để gợi ý tiếp theo)
    by_first: HashMap<String, Vec<usize>>,
    /// first_word → danh sách phrase bắt đầu bằng first_word (dùng khi input 1 từ)
    phrases: Vec<Phrase>,
    /// set hợp lệ (lowercase)
    phrase_set: std::collections::HashSet<String>,
}

impl PhraseIndex {
    fn load(path: &PathBuf) -> Result<Self, String> {
        let content = fs::read_to_string(path)
            .map_err(|e| format!("Không đọc được TuVung.txt: {e}"))?;

        let mut phrases: Vec<Phrase> = Vec::with_capacity(60_000);
        let mut phrase_set = std::collections::HashSet::new();
        let mut by_first: HashMap<String, Vec<usize>> = HashMap::new();

        for line in content.lines() {
            let text = normalize(line);
            if text.is_empty() {
                continue;
            }
            let parts: Vec<&str> = text.split_whitespace().collect();
            if parts.len() != 2 {
                // Bỏ qua từ đơn và cụm > 2 từ
                continue;
            }
            let first = parts[0].to_string();
            let last = parts[1].to_string();
            let idx = phrases.len();
            phrase_set.insert(text.clone());
            by_first.entry(first.clone()).or_default().push(idx);
            phrases.push(Phrase { text, last });
        }

        Ok(Self { by_first, phrases, phrase_set })
    }

    /// Kiểm tra cụm 2 từ có hợp lệ không
    fn is_valid(&self, text: &str) -> bool {
        self.phrase_set.contains(text)
    }

    /// Gợi ý cụm tiếp theo (input 2 từ): lấy last_word → các cụm bắt đầu bằng last_word
    fn suggest_next(&self, phrase: &str, top_n: usize, mode: Mode) -> Vec<RankedPhrase> {
        let parts: Vec<&str> = phrase.split_whitespace().collect();
        if parts.len() != 2 {
            return vec![];
        }
        let last_word = parts[1];
        let indices = match self.by_first.get(last_word) {
            Some(v) => v,
            None => return vec![],
        };
        
        let mut ranked: Vec<RankedPhrase> = indices
            .iter()
            .take(200) // Lấy tối đa 200 cụm để rank
            .map(|&i| {
                let p = &self.phrases[i];
                let next_count = self.by_first.get(&p.last).map(|v| v.len()).unwrap_or(0);
                RankedPhrase {
                    phrase: p.text.clone(),
                    next_count,
                }
            })
            .collect();

        match mode {
            Mode::Bottom => ranked.sort_by_key(|r| (std::cmp::Reverse(r.next_count))),
            _ => ranked.sort_by_key(|r| r.next_count),
        }

        ranked.truncate(top_n);
        ranked
    }

    /// Gợi ý cụm từ khi input 1 từ:
    ///   - Tìm các cụm bắt đầu bằng first_word
    ///   - Rank theo số lượng cụm tiếp theo (next_count) tăng dần → ít nhánh trước
    fn suggest_from_first(&self, first_word: &str, top_n: usize, mode: Mode) -> Vec<RankedPhrase> {
        let indices = match self.by_first.get(first_word) {
            Some(v) => v,
            None => return vec![],
        };
        let mut ranked: Vec<RankedPhrase> = indices
            .iter()
            .take(200)
            .map(|&i| {
                let p = &self.phrases[i];
                let next_count = self.by_first.get(&p.last).map(|v| v.len()).unwrap_or(0);
                RankedPhrase {
                    phrase: p.text.clone(),
                    next_count,
                }
            })
            .collect();
        
        match mode {
            Mode::Bottom => ranked.sort_by_key(|r| (std::cmp::Reverse(r.next_count))),
            _ => ranked.sort_by_key(|r| r.next_count),
        }
        
        ranked.truncate(top_n);
        ranked
    }
}

#[derive(Clone)]
struct RankedPhrase {
    phrase: String,
    next_count: usize,
}

// ─────────────────────────────────────────────
//  NORMALIZE
// ─────────────────────────────────────────────

fn normalize(s: &str) -> String {
    s.trim()
        .nfc() // Chuẩn hóa Unicode về dạng Composed (NFC)
        .to_string()
        .to_lowercase()
        .split_whitespace()
        .collect::<Vec<_>>()
        .join(" ")
}

// ─────────────────────────────────────────────
//  PICK FUNCTIONS
// ─────────────────────────────────────────────

#[derive(Clone, Copy, PartialEq)]
enum Mode {
    Smart(f64), // f64 là tỉ lệ chọn ngẫu nhiên (0.0 - 1.0)
    Top,        // 100% top tier
    Bottom,     // 100% bottom tier (highest branches)
}

/// Pick từ danh sách gợi ý 2-từ
fn pick_two_word(candidates: &[RankedPhrase], mode: Mode) -> Option<String> {
    if candidates.is_empty() {
        return None;
    }
    let mut rng = rand::rng();
    match mode {
        Mode::Top => {
            let best = candidates[0].next_count;
            let top: Vec<_> = candidates.iter().filter(|r| r.next_count == best).collect();
            Some(top.choose(&mut rng)?.phrase.clone())
        }
        Mode::Smart(chance) => {
            if rand::random::<f64>() < chance {
                Some(candidates.choose(&mut rng)?.phrase.clone())
            } else {
                let best = candidates[0].next_count;
                let top: Vec<_> = candidates.iter().filter(|r| r.next_count == best).collect();
                Some(top.choose(&mut rng)?.phrase.clone())
            }
        }
        Mode::Bottom => {
            // Ưu tiên từ có hơn 4 cách nối tiếp
            let rich: Vec<_> = candidates.iter().filter(|r| r.next_count > 4).collect();
            if !rich.is_empty() {
                Some(rich.choose(&mut rng)?.phrase.clone())
            } else {
                // Không có từ nào > 4 cách nối → chọn ngẫu nhiên
                Some(candidates.choose(&mut rng)?.phrase.clone())
            }
        }
    }
}

/// Pick từ danh sách ranked (1-từ input)
fn pick_one_word(ranked: &[RankedPhrase], mode: Mode) -> Option<String> {
    if ranked.is_empty() {
        return None;
    }
    let mut rng = rand::rng();
    match mode {
        Mode::Top => {
            // top tier = nhóm có next_count nhỏ nhất
            let best = ranked[0].next_count;
            let top: Vec<_> = ranked.iter().filter(|r| r.next_count == best).collect();
            Some(top.choose(&mut rng)?.phrase.clone())
        }
        Mode::Smart(chance) => {
            if rand::random::<f64>() < chance {
                Some(ranked.choose(&mut rng)?.phrase.clone())
            } else {
                let best = ranked[0].next_count;
                let top: Vec<_> = ranked.iter().filter(|r| r.next_count == best).collect();
                Some(top.choose(&mut rng)?.phrase.clone())
            }
        }
        Mode::Bottom => {
            // Ưu tiên từ có hơn 4 cách nối tiếp
            let rich: Vec<_> = ranked.iter().filter(|r| r.next_count > 4).collect();
            if !rich.is_empty() {
                Some(rich.choose(&mut rng)?.phrase.clone())
            } else {
                // Không có từ nào > 4 cách nối → chọn ngẫu nhiên
                Some(ranked.choose(&mut rng)?.phrase.clone())
            }
        }
    }
}

// ─────────────────────────────────────────────
//  SHARED STATE giữa clipboard-thread và GUI
// ─────────────────────────────────────────────

struct AppState {
    mode: Mode,
    status: String,
    last_written: Option<String>,
    paused: bool,
}

impl Default for AppState {
    fn default() -> Self {
        Self {
            mode: Mode::Smart(0.25),
            status: "Sẵn sàng.".into(),
            last_written: None,
            paused: false,
        }
    }
}

// ─────────────────────────────────────────────
//  CLIPBOARD POLLING (chạy trong thread riêng)
// ─────────────────────────────────────────────

fn start_clipboard_thread(index: Arc<PhraseIndex>, state: Arc<Mutex<AppState>>) {
    thread::spawn(move || {
        let mut clipboard = match Clipboard::new() {
            Ok(c) => c,
            Err(e) => {
                let mut s = state.lock().unwrap();
                s.status = format!("❌ Lỗi clipboard: {e}");
                return;
            }
        };

        let mut last_seen: Option<String> = None;

        loop {
            thread::sleep(Duration::from_millis(POLL_MS));

            let current = clipboard.get_text().unwrap_or_default();
            let current_norm = normalize(&current);

            if Some(&current_norm) == last_seen.as_ref() {
                continue;
            }

            // Kiểm tra xem đây có phải nội dung mình vừa ghi không
            {
                let s = state.lock().unwrap();
                if let Some(ref lw) = s.last_written {
                    if &current_norm == lw {
                        last_seen = Some(current_norm.clone());
                        continue;
                    }
                }
            }

            last_seen = Some(current_norm.clone());

            if current_norm.is_empty() {
                continue;
            }

            // --- Lệnh chuyển chế độ & Tạm dừng ---
            if current_norm == "!" {
                let mut s = state.lock().unwrap();
                s.mode = Mode::Top;
                s.paused = false;
                s.status = "✅ Đã chuyển sang chế độ TOP TIER ONLY".into();
                continue;
            }
            if current_norm == "%!" {
                let mut s = state.lock().unwrap();
                s.mode = Mode::Bottom;
                s.paused = false;
                s.status = "✅ Đã chuyển sang chế độ BOTTOM TIER (Top cuối)".into();
                continue;
            }
            if current_norm.starts_with('%') {
                let mut s = state.lock().unwrap();
                let chance = if current_norm.len() > 1 {
                    match current_norm[1..].parse::<f64>() {
                        Ok(val) => val.clamp(0.0, 100.0) / 100.0,
                        Err(_) => 0.25,
                    }
                } else {
                    0.25
                };
                s.mode = Mode::Smart(chance);
                s.paused = false;
                let pct_top = (1.0 - chance) * 100.0;
                let pct_rand = chance * 100.0;
                s.status = format!("✅ SMART RANDOM: {:.0}% Top / {:.0}% Random", pct_top, pct_rand);
                continue;
            }
            if current_norm == "." {
                let mut s = state.lock().unwrap();
                s.paused = !s.paused; // Toggle trạng thái
                if s.paused {
                    s.status = "⏸️ ĐÃ TẠM DỪNG gợi ý.".into();
                } else {
                    s.status = "▶️ ĐÃ TIẾP TỤC gợi ý.".into();
                }
                continue;
            }

            // Kiểm tra nếu đang tạm dừng
            {
                if state.lock().unwrap().paused {
                    continue;
                }
            }

            // --- Gợi ý ---
            let parts: Vec<&str> = current_norm.split_whitespace().collect();
            if parts.len() > 2 {
                let mut s = state.lock().unwrap();
                s.status = "Nội dung quá dài (> 2 từ), bỏ qua.".into();
                continue;
            }

            let mode = state.lock().unwrap().mode;

            let suggestion: Option<String> = if parts.len() == 1 {
                let ranked = index.suggest_from_first(parts[0], TOP_N, mode);
                pick_one_word(&ranked, mode)
            } else {
                // Logic cụm 2 từ:
                // Thử tìm gợi ý từ từ thứ 2 bất kể cụm này có trong từ điển không
                let candidates = index.suggest_next(&current_norm, TOP_N, mode);
                if candidates.is_empty() {
                    let mut s = state.lock().unwrap();
                    s.status = format!("Không tìm thấy từ bắt đầu bằng \"{}\"", parts[1]);
                    None
                } else {
                    pick_two_word(&candidates, mode)
                }
            };

            match suggestion {
                Some(ref sug) if sug != &current_norm => {
                    let short = if sug.len() <= 48 {
                        sug.clone()
                    } else {
                        format!("{}…", &sug[..45])
                    };
                    // Ghi vào clipboard
                    let _ = clipboard.set_text(sug.clone());
                    let mut s = state.lock().unwrap();
                    s.last_written = Some(sug.clone());
                    s.status = format!("📋 Đã copy gợi ý: {short}");
                    last_seen = Some(normalize(sug));
                }
                Some(_) => {
                    let mut s = state.lock().unwrap();
                    s.status = "Trùng gợi ý — clipboard giữ nguyên.".into();
                }
                None => {
                    let mut s = state.lock().unwrap();
                    s.status = format!("Không có gợi ý cho: \"{}\"", current_norm);
                }
            }
        }
    });
}

// ─────────────────────────────────────────────
//  ICON LOADER
// ─────────────────────────────────────────────

/// Tạo egui IconData từ raw RGBA bytes đã nhúng sẵn (không cần decode)
fn load_icon() -> egui::IconData {
    egui::IconData {
        rgba: ICON_RGBA.to_vec(),
        width: ICON_SIZE,
        height: ICON_SIZE,
    }
}

// ─────────────────────────────────────────────
//  FONT SETUP
// ─────────────────────────────────────────────

/// Load Segoe UI từ thư mục Windows Fonts — hỗ trợ đầy đủ tiếng Việt.
/// Nếu không tìm được thì giữ font mặc định của egui.
fn setup_fonts(ctx: &egui::Context) {
    // Các đường dẫn font ưu tiên
    let font_candidates: &[&str] = &[
        r"C:\Windows\Fonts\segoeui.ttf",
        r"C:\Windows\Fonts\arial.ttf",
        r"C:\Windows\Fonts\tahoma.ttf",
    ];

    let font_data = font_candidates
        .iter()
        .find_map(|path| std::fs::read(path).ok());

    if let Some(data) = font_data {
        let mut fonts = egui::FontDefinitions::default();

        fonts.font_data.insert(
            "viet_font".to_owned(),
            egui::FontData::from_owned(data).into(),
        );

        // Đặt làm font đầu tiên cho tất cả các style
        fonts
            .families
            .entry(egui::FontFamily::Proportional)
            .or_default()
            .insert(0, "viet_font".to_owned());

        fonts
            .families
            .entry(egui::FontFamily::Monospace)
            .or_default()
            .push("viet_font".to_owned());

        ctx.set_fonts(fonts);
    }
}

// ─────────────────────────────────────────────
//  GUI (eframe / egui)
// ─────────────────────────────────────────────

struct App {
    state: Arc<Mutex<AppState>>,
}

impl eframe::App for App {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        // Luôn repaint để status được cập nhật ngay
        ctx.request_repaint_after(Duration::from_millis(300));

        egui::CentralPanel::default().show(ctx, |ui| {
            ui.add_space(8.0);

            // Tiêu đề
            ui.horizontal(|ui| {
                ui.add_space(6.0);
                ui.heading("Gợi ý Nối Từ");
            });

            ui.add_space(6.0);
            ui.separator();
            ui.add_space(6.0);

            // Hướng dẫn
            ui.label("Copy 1 từ hoặc cụm 2 từ → clipboard tự động thay bằng gợi ý.");
            ui.label("  • Copy \"!\"  → 🎯 chế độ TOP TIER ONLY");
            ui.label("  • Copy \"%\"  → 🎲 chế độ SMART RANDOM");
            ui.label("  • Copy \"%!\" → 🔥 chế độ BOTTOM TIER (Top cuối)");
            ui.label("  • Copy \".\"  → ⏸️ TẠM DỪNG / TIẾP TỤC");
            ui.label("  • Copy \"?\"  → hiển thị hướng dẫn này");

            ui.add_space(8.0);
            ui.separator();
            ui.add_space(6.0);

            // Chế độ hiện tại
            let (mode_text, mode_color) = {
                let s = self.state.lock().unwrap();
                if s.paused {
                    ("⏸️ ĐANG TẠM DỪNG".to_string(), egui::Color32::from_rgb(180, 180, 180))
                } else {
                    match s.mode {
                        Mode::Top   => ("🎯 TOP TIER ONLY".to_string(),                  egui::Color32::from_rgb(220, 80, 80)),
                        Mode::Smart(chance) => (
                            format!("🎲 SMART RANDOM ({:.0}% top / {:.0}% any)", (1.0 - chance) * 100.0, chance * 100.0),
                            egui::Color32::from_rgb(0, 120, 210)
                        ),
                        Mode::Bottom => ("🔥 BOTTOM TIER (Top cuối)".to_string(),           egui::Color32::from_rgb(210, 120, 0)),
                    }
                }
            };
            ui.horizontal(|ui| {
                ui.label("Chế độ:");
                ui.colored_label(mode_color, mode_text);
            });

            ui.add_space(6.0);

            // Status
            let status = self.state.lock().unwrap().status.clone();
            ui.horizontal(|ui| {
                ui.label("Trạng thái:");
                ui.label(&status);
            });

            ui.add_space(10.0);
            ui.separator();
            ui.add_space(6.0);

            // Nút thoát
            ui.horizontal(|ui| {
                ui.add_space(ui.available_width() / 2.0 - 30.0);
                if ui.button("  Thoát  ").clicked() {
                    ctx.send_viewport_cmd(egui::ViewportCommand::Close);
                }
            });
        });
    }
}

// ─────────────────────────────────────────────
//  MAIN
// ─────────────────────────────────────────────

fn main() {
    // Đường dẫn TuVung.txt: cùng thư mục với exe, hoặc thư mục cha
    let exe_dir = std::env::current_exe()
        .ok()
        .and_then(|p| p.parent().map(|d| d.to_path_buf()))
        .unwrap_or_default();

    // Tìm TuVung.txt theo thứ tự ưu tiên
    let candidates = [
        exe_dir.join("TuVung.txt"),
        exe_dir.join("../TuVung.txt"),
        exe_dir.join("../../TuVung.txt"),
        PathBuf::from(r"d:\Bot_Noi_Tu_Viet\TuVung.txt"),
    ];

    let dict_path = candidates
        .iter()
        .find(|p| p.exists())
        .cloned()
        .unwrap_or_else(|| PathBuf::from("TuVung.txt"));

    // Load index
    let index = match PhraseIndex::load(&dict_path) {
        Ok(idx) => {
            println!(
                "✅ Đã load {} cụm 2 từ từ {:?}",
                idx.phrases.len(),
                dict_path
            );
            Arc::new(idx)
        }
        Err(e) => {
            // Hiển thị lỗi qua messagebox Windows nếu có thể
            eprintln!("❌ {e}");
            // Thử show error window
            let _ = eframe::run_native(
                "Lỗi",
                eframe::NativeOptions {
                    viewport: egui::ViewportBuilder::default()
                        .with_inner_size([400.0, 100.0])
                        .with_resizable(false),
                    ..Default::default()
                },
                Box::new(move |_cc| {
                    Ok(Box::new(ErrorApp { msg: e.clone() }))
                }),
            );
            return;
        }
    };

    let state = Arc::new(Mutex::new(AppState::default()));

    // Bắt đầu thread theo dõi clipboard
    start_clipboard_thread(Arc::clone(&index), Arc::clone(&state));

    // Khởi động GUI
    let mut viewport = egui::ViewportBuilder::default()
        .with_title("Goi y Noi Tu – Clipboard")
        .with_inner_size([480.0, 220.0])
        .with_resizable(false)
        .with_maximize_button(false);

    // Gắn icon (window + taskbar)
    viewport = viewport.with_icon(Arc::new(load_icon()));

    let options = eframe::NativeOptions {
        viewport,
        ..Default::default()
    };

    let _ = eframe::run_native(
        "Goi y Noi Tu",
        options,
        Box::new(move |cc| {
            setup_fonts(&cc.egui_ctx);
            Ok(Box::new(App { state }))
        }),
    );
}

// ─── App hiển thị lỗi load ───
struct ErrorApp {
    msg: String,
}
impl eframe::App for ErrorApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        egui::CentralPanel::default().show(ctx, |ui| {
            ui.colored_label(egui::Color32::RED, &self.msg);
            if ui.button("Đóng").clicked() {
                ctx.send_viewport_cmd(egui::ViewportCommand::Close);
            }
        });
    }
}
