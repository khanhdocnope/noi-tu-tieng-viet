# Đặc tả Tích hợp Bàn phím Tiếng Việt Tiêu chuẩn

## 1. Giao diện QWERTY (`standard-qwerty-layout`)

### Cấu trúc hàng phím
- **Hàng 1**: q w e r t y u i o p
- **Hàng 2**: a s d f g h j k l
- **Hàng 3**: [Shift] z x c v b n m [Delete]
- **Hàng 4**: [123] [Dấu phẩy] [Dấu cách] [Dấu chấm] [Enter]

### Toggle Bot
Nút Logo ở góc trên bên trái thanh trạng thái sẽ dùng để chuyển đổi giữa `Bot Control View` và `QWERTY View`.

## 2. Bộ gõ Telex (`vietnamese-telex-engine`)

### Quy tắc bỏ dấu cơ bản
- `aa` -> `â`, `aw` -> `ă`, `ee` -> `ê`, `oo` -> `ô`, `ow` -> `ơ`, `uw` -> `ư`, `dd` -> `đ`.
- `s` -> sắc, `f` -> huyền, `r` -> hỏi, `x` -> ngã, `j` -> nặng.
- `w` ở cuối từ -> `ư` hoặc `ơ` tùy ngữ cảnh (hoặc đơn giản là `w` -> `ư`).

### Xử lý Input
Khi gõ phím, ứng dụng sẽ kiểm tra ký tự cuối cùng trong `InputConnection` để thực hiện tổ hợp dấu.

## 3. Trạng thái Bot
Dù ở chế độ QWERTY, `ClipboardManager` vẫn hoạt động. Khi người dùng copy một từ, thanh trạng thái (tvStatus) vẫn sẽ hiển thị từ gợi ý nhưng không tự động gõ nếu chưa bật chế độ Bot.
