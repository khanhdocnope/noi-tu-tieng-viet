## Context

Chúng ta cần tích hợp chế độ Bottom Tier vào ứng dụng Android một cách mượt mà, giữ nguyên phong cách Gaming Dark hiện tại.

## Goals / Non-Goals

**Goals:**
- Thêm chế độ chọn từ "dễ nhất" vào logic Android.
- Hiển thị Badge "TOP CUỐI" với màu cam (lửa) đặc trưng.
- Cho phép chuyển đổi vòng tròn giữa: Cực phẩm -> Thông minh -> Top Cuối.

**Non-Goals:**
- Không làm thay đổi tốc độ xử lý của bàn phím.
- Không thay đổi cấu trúc file `TuVung.txt`.

## Decisions

- **Logic chọn từ**: Thay vì chọn từ có `nextCount` nhỏ nhất (Top Tier), chúng ta sẽ chọn từ có `nextCount` lớn nhất.
- **Màu sắc UI**: Sử dụng màu Cam (#FF4500) hoặc Vàng Lửa (#FFA500) cho chế độ Bottom Tier để phân biệt với màu Xanh lơ (Cyan) của Smart Random và màu Xanh lá (Green) của Top Tier.
- **Điều khiển**: Cập nhật sự kiện click của `btnMode` trong `BotKeyboardService.kt` để xoay vòng qua 3 chế độ thay vì 2 như trước.

## Risks / Trade-offs

- **Dung lượng UI**: Việc thêm một chế độ có thể làm text trên nút bấm dài hơn, cần căn chỉnh `textSize` để không bị tràn layout trên màn hình nhỏ.
