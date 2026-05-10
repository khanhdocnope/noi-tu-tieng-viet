## Why

Giao diện Android hiện tại đã có khung cơ bản nhưng cần thêm các yếu tố "Premium" và "Gaming" để thu hút người dùng. Ngoài ra, trải nghiệm người dùng (UX) khi thiết lập bàn phím lần đầu cần được tối ưu hóa bằng cách cho phép người dùng thử bàn phím ngay trong ứng dụng.

## What Changes

Hoàn thiện giao diện ứng dụng Android bao gồm:
- **Nâng cấp Banner**: Thêm hiệu ứng phát sáng (glow) và biểu tượng động cho phần banner đầu trang.
- **Thêm Khu vực Thử nghiệm (Test Area)**: Thêm một ô nhập liệu (`EditText`) ở cuối màn hình Cài đặt để người dùng kiểm tra bàn phím sau khi thiết lập.
- **Tối ưu hóa các Step Card**: Cải thiện hiệu ứng hình ảnh khi người dùng thực hiện xong mỗi bước.
- **Hiệu ứng Micro-animations**: Thêm các animation đơn giản như pulsing dot mạnh hơn và hiệu ứng hover giả lập cho các nút bấm.

## Capabilities

### Modified Capabilities
- `android-settings-ui`: Cập nhật `activity_settings.xml` để thêm khu vực test và nâng cấp banner.
- `android-keyboard-ui`: Cập nhật `keyboard_view.xml` để thêm các chi tiết thẩm mỹ.

## Impact

- Chỉnh sửa file layout `activity_settings.xml` và `keyboard_view.xml`.
- Thêm các resource drawable mới cho hiệu ứng phát sáng và bo góc.
- Cập nhật `colors.xml` nếu cần thêm các dải màu gradient.
