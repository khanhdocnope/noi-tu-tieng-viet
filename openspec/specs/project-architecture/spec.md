# Kiến trúc Hệ thống Bot Nối Từ

## Tổng quan
Dự án chia làm 3 thành phần chính:
1. **Dữ liệu (`TuVung.txt`)**: Trái tim của hệ thống.
2. **Backend PC (Rust)**: Xử lý clipboard trên Windows.
3. **Bàn phím Android (Java)**: Tự động gõ phím trên điện thoại.

## Cơ chế Gợi ý Từ
Cả hai phiên bản đều sử dụng chung logic:
- Nhận đầu vào là một từ/cụm từ cuối cùng được sao chép.
- Tìm kiếm trong `TuVung.txt` các cụm từ bắt đầu bằng tiếng cuối cùng của từ đầu vào.
- Phân loại từ tìm được theo độ "hiểm" (những từ khiến đối thủ khó nối tiếp).

## Chi tiết Phiên bản PC
- **Ngôn ngữ**: Rust.
- **Thư viện**: `arboard` (quản lý clipboard), `iced` hoặc `native-windows-gui` (cho giao diện).
- **Luồng**: Chạy ngầm -> Lắng nghe sự kiện Clipboard -> Tìm từ -> Ghi đè vào Clipboard.

## Chi tiết Phiên bản Android
- **Ngôn ngữ**: Java (Android SDK).
- **Thành phần**: `InputMethodService`.
- **Giao diện**: Custom View cho bàn phím với theme Dark Gaming.
- **Luồng**: Khi người dùng copy -> App nhận diện thông qua `ClipboardManager` -> Hiển thị từ gợi ý hoặc tự động gõ (Auto-type) thông qua `getCurrentInputConnection().commitText()`.
