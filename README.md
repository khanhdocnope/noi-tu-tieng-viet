# Bot Nối Từ Việt (Rust Edition) 🦀

> **Lưu ý quan trọng:** Đây là ứng dụng đã được biên dịch (Compiled). Người dùng cuối **KHÔNG CẦN CÀI RUST** hay bất kỳ môi trường lập trình nào vẫn có thể chạy trực tiếp file `.exe`.

Bạn đang vật lộn với trò chơi nối từ tiếng Việt? Đã có giải pháp!
Ứng dụng hỗ trợ trò chơi nối từ tiếng Việt thông qua việc theo dõi Clipboard. Khi bạn copy một từ hoặc cụm 2 từ, ứng dụng sẽ tự động gợi ý từ tiếp theo dựa trên từ điển và ghi đè vào clipboard của bạn.

Phiên bản Rust này được tối ưu hóa về tốc độ, hiệu suất và dung lượng RAM so với phiên bản Python cũ.

##  Tính năng chính

- **Theo dõi Clipboard**: Tự động nhận diện từ vựng khi bạn nhấn `Ctrl + C`.
- **Gợi ý thông minh**: Dựa trên tập dữ liệu `TuVung.txt` với hơn 53,000 cụm từ.
- **Ba chế độ hoạt động**:
    - 🎯 **TOP TIER ONLY**: Luôn chọn từ "hiểm" nhất (ít nhánh nối tiếp nhất). (Copy dấu `!`).
    - 🎲 **SMART RANDOM**: Mặc định 75% chọn từ tốt nhất, 25% chọn ngẫu nhiên. (Copy dấu `%`). 
        *   **Mẹo**: Bạn có thể tùy chỉnh tỷ lệ bằng cách copy `%a` (với `a` là % ngẫu nhiên). Ví dụ: copy `%10` để có 90% cực phẩm và 10% ngẫu nhiên.
    - ⏸️ **PAUSE**: Tạm dừng mọi gợi ý (Copy dấu `.`). Để tiếp tục, hãy copy lại các lệnh chế độ hoặc một từ mới.
- **Giao diện nhẹ nhàng**: Hiển thị trạng thái hoạt động và chế độ hiện tại qua GUI tối giản.

##  Hướng dẫn sử dụng

1. Chạy file `clipboard_autosuggest.exe`.
2. Đảm bảo file `TuVung.txt` nằm cùng thư mục với file chạy.
3. Khi chơi, bạn chỉ cần copy từ của đối thủ:
    - Nếu bạn copy **1 từ**: Bot sẽ tìm cụm từ bắt đầu bằng từ đó.
    - Nếu bạn copy **cụm 2 từ**: Bot sẽ lấy từ cuối để tìm cụm từ tiếp theo.
4. Nhấn `Ctrl + V` để dán kết quả bot đã gợi ý.

##  Hướng dẫn Build từ Source

Nếu bạn muốn tự compile lại ứng dụng:

1. Cài đặt **Rust** (https://rustup.rs/).
2. Di chuyển vào thư mục dự án:
   ```bash
   cd clipboard_autosuggest
   ```
3. Build phiên bản chính thức (Release):
   ```bash
   cargo build --release
   ```
4. File chạy sẽ nằm tại: `target/release/clipboard_autosuggest.exe`.
5. Sau khi build hoàn tất, bạn có thể di chuyển file `clipboard_autosuggest.exe` ra thư mục ngoài để sử dụng.
## 📁Cấu trúc thư mục

- `src/main.rs`: Toàn bộ logic xử lý clipboard và gợi ý.
- `TuVung.txt`: Từ điển dữ liệu đầu vào.
- `Cargo.toml`: Quản lý dependencies của Rust.

## 📝 Lưu ý

- Ứng dụng chỉ hoạt động với các cụm từ tiếng Việt có dấu/không dấu chuẩn.
- Tự động bỏ qua các nội dung dài hơn 2 từ để tránh xử lý nhầm văn bản thông thường.
- Có thể tồn tại các lỗi không mong muốn, vui lòng báo cáo cho tôi nếu bạn gặp phải.
- Có thể một số từ sẽ không có trong từ điển do dữ liệu của các bot là khác nhau, mong bạn thông cảm.
