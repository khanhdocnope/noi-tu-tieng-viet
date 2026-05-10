## Context

Giao diện Android hiện tại sử dụng tông màu Dark (Gaming). Chúng ta sẽ nâng cấp nó lên tầm "Premium" bằng cách tập trung vào các chi tiết nhỏ (micro-details) và trải nghiệm người dùng thực tế.

## Goals / Non-Goals

**Goals:**
- Tăng tính thẩm mỹ cho Banner và Step Cards.
- Thêm tính năng "Live Test" ngay trong ứng dụng cài đặt.
- Tối ưu hóa phản hồi thị giác (visual feedback) trên bàn phím.

**Non-Goals:**
- Thay đổi logic gõ phím hoặc tìm từ.
- Thêm các cài đặt cấu hình phức tạp khác.

## Decisions

- **Test Area Design**: Đặt ở cuối màn hình ScrollView của Settings, sử dụng kiểu dáng một ô chat giả lập để người dùng cảm thấy quen thuộc.
- **Banner Glow**: Sử dụng một `ShapeDrawable` với gradient hoặc layer-list để tạo hiệu ứng phát sáng Cyan nhẹ xung quanh logo.
- **Card Polishing**: Thêm các đường kẻ (divider) cực mảnh và gradient nhẹ cho card để tạo chiều sâu (depth).
- **Keyboard Polish**: Tăng kích thước chữ hiển thị trạng thái và làm cho điểm chỉ báo (dot) nhấp nháy mượt mà hơn bằng `ObjectAnimator`.

## Risks / Trade-offs

- **Performance**: Việc thêm nhiều drawable phức tạp và animation có thể ảnh hưởng nhẹ đến các máy Android cấu hình cực thấp. Tuy nhiên, với XML native thì ảnh hưởng này là tối thiểu.
