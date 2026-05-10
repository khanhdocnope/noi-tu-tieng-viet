# Đặc tả Nâng cấp Giao diện Android

## Giao diện Cài đặt (`activity_settings.xml`)

### 1. Banner Nâng cao
- Thêm một `FrameLayout` bao quanh Logo để hỗ trợ hiệu ứng bóng đổ hoặc glow.
- Sử dụng màu `accent_cyan` kết hợp với `accent_purple` để tạo dải màu gradient cho text "Bot Nối Từ".

### 2. Khu vực Chơi thử (Test Area)
- Thêm một `LinearLayout` ở dưới cùng.
- Bên trong chứa một `EditText` với style bo tròn (`bg_status`).
- Hint text: "Thử gõ hoặc dán vào đây..."
- Giúp người dùng kiểm tra xem bàn phím đã được kích hoạt đúng chưa.

## Giao diện Bàn phím (`keyboard_view.xml`)

### 1. Hiệu ứng Badge
- Cập nhật `tvModeBadge` để có padding tốt hơn và font chữ đậm hơn.
- Thêm hiệu ứng gạch chân hoặc viền neon mỏng.

### 2. Trạng thái hoạt động
- Làm cho `viewDot` có kích thước lớn hơn một chút (10dp).
- Cấu hình để `tvStatus` hiển thị font chữ Mono (nếu có) hoặc tăng `letterSpacing` để tạo cảm giác công nghệ.

## Resource mới cần tạo
- `bg_banner_glow.xml`: Layer-list tạo bóng lấp lánh.
- `bg_edittext_test.xml`: Bo góc 24dp cho ô nhập liệu thử nghiệm.
