# Đặc tả Chế độ Bottom Tier (Top Cuối) trên Android

## 1. Logic Engine (`bottom-tier-logic`)

### Mô tả
Tìm kiếm các từ nối tiếp và ưu tiên những từ có số lượng "từ nối tiếp của nó" là lớn nhất.

### Scenarios
#### Chọn từ dễ nhất
- **Input**: Từ "Hoa"
- **Candidates**: ["Hoa hồng" (10 từ nối), "Hoa ly" (2 từ nối), "Hoa sữa" (0 từ nối)]
- **Output (Bottom Tier)**: "Hoa hồng" (vì có 10 lựa chọn, đối thủ dễ nối tiếp nhất).

## 2. Quản lý Chế độ (`bot-mode-management`)

### Cập nhật Enum
Thêm `BOTTOM_TIER` vào enum `Mode` trong `BotKeyboardService.kt`.

### Cập nhật Logic Xoay vòng
`currentMode` sẽ thay đổi theo thứ tự: `TOP_TIER` -> `SMART_RANDOM` -> `BOTTOM_TIER` -> `TOP_TIER`.

## 3. Giao diện người dùng (`android-keyboard-ui`)

### Badge mới
- **ID**: `badge_bottom.xml` (Drawable mới).
- **Text**: "🔥 TOP CUỐI".
- **Color**: `@color/accent_amber` (hoặc màu cam mới).

### Cập nhật Layout
Đảm bảo `tvModeBadge` và `btnMode` cập nhật đúng text và màu sắc khi chuyển sang chế độ mới.
