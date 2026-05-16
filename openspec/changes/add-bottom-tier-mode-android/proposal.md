## Why

Hiện tại, phiên bản Android chỉ có hai chế độ: Cực phẩm (Top Tier) và Thông minh (Smart Random). Phiên bản PC có thêm chế độ "Bottom Tier" (Top cuối) giúp người dùng có thể nhường đối thủ hoặc kéo dài trận đấu. Việc thêm tính năng này vào Android sẽ đồng nhất trải nghiệm giữa hai nền tảng.

## What Changes

Thêm chế độ "Top Cuối" (Bottom Tier) vào ứng dụng Android:
- Cập nhật logic tìm từ: Thêm khả năng chọn những từ có nhiều từ nối tiếp nhất (từ "dễ").
- Cập nhật Giao diện Bàn phím: Thêm nút bấm hoặc cơ chế chuyển đổi sang chế độ Bottom Tier.
- Cập nhật UI Badge: Thêm badge hiển thị trạng thái "Top Cuối" với màu sắc riêng biệt (ví dụ: Màu Cam/Lửa như trên PC).

## Capabilities

### New Capabilities
- `bottom-tier-logic`: Logic tìm kiếm và phân loại các từ "dễ" (có nhiều lựa chọn nối tiếp).

### Modified Capabilities
- `android-keyboard-ui`: Cập nhật giao diện bàn phím để hỗ trợ hiển thị và chuyển đổi sang chế độ mới.
- `bot-mode-management`: Cập nhật hệ thống quản lý các Mode trong `BotKeyboardService.kt`.

## Impact

- Chỉnh sửa `BotKeyboardService.kt` để thêm Enum Mode mới và cập nhật logic chọn từ.
- Chỉnh sửa `keyboard_view.xml` và `activity_settings.xml` để thêm các thành phần UI tương ứng.
- Thêm drawable resource mới cho Badge "Top Cuối".
