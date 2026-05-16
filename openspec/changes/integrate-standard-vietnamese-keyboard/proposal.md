## Why

Hiện tại, ứng dụng Bot Nối Từ chỉ là một bảng điều khiển (Controller), không có khả năng gõ văn bản thông thường. Điều này khiến người dùng dễ bị phát hiện khi không thể chat bình thường với đối thủ. Tích hợp một bộ gõ Tiếng Việt tiêu chuẩn sẽ giúp:
- Tăng tính "ngụy trang" (Stealth).
- Tiện dụng hơn khi chơi game mà không cần chuyển đổi bàn phím liên tục.
- Giữ vững ưu thế của Bot khi cần thiết chỉ bằng một nút bấm bí mật.

## What Changes

- **Hệ thống Layout kép**: Thêm layout QWERTY đầy đủ bên dưới thanh trạng thái của Bot.
- **Bộ gõ Tiếng Việt (Telex)**: Tích hợp logic xử lý bỏ dấu tiếng Việt cơ bản.
- **Nút chuyển đổi chuyên biệt**: Một nút (ví dụ: Logo ứng dụng hoặc icon nhỏ) để bật/tắt nhanh giữa chế độ "Gõ tay" và "Bot tự động".
- **Lắng nghe nền**: Clipboard vẫn được theo dõi liên tục dù đang ở chế độ gõ tay, để khi bật Bot là có đáp án ngay.

## Capabilities

### New Capabilities
- `standard-qwerty-layout`: Giao diện bàn phím QWERTY đầy đủ.
- `vietnamese-telex-engine`: Logic xử lý tổ hợp phím để tạo chữ tiếng Việt.

### Modified Capabilities
- `bot-keyboard-service`: Cập nhật `InputMethodService` để xử lý sự kiện phím (onKey) thay vì chỉ click nút.
- `ui-state-management`: Quản lý trạng thái hiển thị giữa Bàn phím thường và Bảng điều khiển Bot.

## Impact

- Chỉnh sửa lớn `keyboard_view.xml` để thêm các hàng phím (Row 1, 2, 3).
- Bổ sung logic xử lý phím trong `BotKeyboardService.kt`.
- Cần thêm các resource về ký tự và nhãn phím.
