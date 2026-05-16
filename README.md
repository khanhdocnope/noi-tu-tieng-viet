# Bot Nối Từ Việt (Ultimate Edition) 

> **Lưu ý quan trọng:** Dự án bao gồm phiên bản máy tính (Rust) và điện thoại (Android). Cả hai đều được thiết kế để tối ưu hóa tốc độ và hiệu suất.

Bạn đang vật lộn với trò chơi nối từ tiếng Việt? Đã có giải pháp "tận răng"! Ứng dụng hỗ trợ trò chơi nối từ thông qua việc theo dõi Clipboard. Khi bạn copy một từ, ứng dụng sẽ tự động gợi ý từ tiếp theo và ghi đè vào clipboard (PC) hoặc gõ thẳng vào ô nhập liệu (Android).

---

##  Phiên bản Máy tính (Rust)

Được tối ưu hóa cực cao về tốc độ và dung lượng RAM (chỉ tốn ~2-5MB RAM).

### Tính năng:
- **Theo dõi Clipboard**: Tự động nhận diện khi nhấn `Ctrl + C`.
- **Ba chế độ hoạt động**:
    - 🎯 **TOP TIER**: Chọn từ "hiểm" nhất. (Copy `!`).
    - 🎲 **SMART RANDOM**: 75% tốt nhất, 25% ngẫu nhiên. (Copy `%`).
    - ⏸️ **PAUSE**: Tạm dừng. (Copy `.`).
- **Giao diện**: GUI tối giản, hiện đại.

### Hướng dẫn nhanh:
1. Chạy `clipboard_autosuggest.exe` (cùng thư mục với `TuVung.txt`).
2. Copy từ đối thủ -> Nhấn `Ctrl + V` để dán đáp án.

---

##  Phiên bản Android (Premium Keyboard)

Đây là phiên bản **"đẳng cấp"** với giao diện **Dark Gaming UI** hiện đại, hoạt động như một bàn phím ảo chuyên dụng.

###  Điểm nổi bật:
- **Giao diện Đẳng cấp**: Phong cách Gaming tối sâu, hiệu ứng Neon Cyan, Purple và LED indicator động.
- **Tự động điền (Auto-Type)**: Khi bạn copy từ của đối thủ, bàn phím sẽ **tự động gõ thẳng đáp án** vào ô chat.
- **Bàn phím Ẩn danh (Stealth Mode)**: Tích hợp bàn phím Tiếng Việt QWERTY để nhắn tin bình thường mà không bị phát hiện.
- **Mode Badge**: Hiển thị trạng thái hoạt động (Cực phẩm/Thông minh/Top cuối) ngay trên bàn phím.

###  Tải về và Hướng dẫn:
- **Tải APK**: Xem tại tab **Actions** (Bản build Android CI mới nhất).
- **Hướng dẫn chi tiết**: Đọc tại [**apk/apk.md**](./apk/apk.md) để biết cách kích hoạt và mẹo sử dụng.

---

## Cấu trúc dự án
- `clipboard_autosuggest/`: Source code phiên bản Rust (PC).
- `AndroidKeyboard/`: Source code ứng dụng bàn phím Android.
- `TuVung.txt`: Từ điển hơn 53,000 cụm từ tiếng Việt.

##  Giấy phép (License)
Dự án được phân phối dưới giấy phép [MIT License](LICENSE).

---
*Ghi chú: Ứng dụng chỉ dùng cho mục đích giải trí và học tập. Vui lòng không dùng để gây ức chế cho người chơi khác!*
