## Context

Chúng ta cần một bàn phím trông giống Gboard hoặc SwiftKey nhưng có "vũ khí bí mật" bên trong.

## Goals / Non-Goals

**Goals:**
- Người dùng có thể gõ tiếng Việt Telex bình thường.
- Có nút chuyển sang chế độ "Bot Control" (layout cũ).
- Layout QWERTY phải tối giản, dễ sử dụng.

**Non-Goals:**
- Không hỗ trợ gõ vuốt (Swipe typing).
- Không hỗ trợ gợi ý từ (Auto-correct/Prediction) cho chế độ gõ tay (để giữ ứng dụng nhẹ).

## Decisions

- **Layout**: Sử dụng `LinearLayout` hoặc `GridLayout` để tạo các hàng phím QWERTY.
- **Stealth Toggle**: Sử dụng nút chuyển chế độ (Mode Button) hiện tại hoặc một icon logo ở góc bàn phím để chuyển đổi layout.
- **Telex Engine**: Sử dụng logic so khớp chuỗi (String mapping) đơn giản cho Telex (ví dụ: a+a=â, a+w=ă, s=sắc...).
- **Theme**: Giữ nguyên theme "Gaming Dark" cho cả bàn phím thường để đồng nhất thẩm mỹ.

## Architecture

1.  **View Layer**: `keyboard_view.xml` sẽ chứa 2 FrameLayout:
    *   `bot_control_layout`: Chứa 3 nút Cực phẩm, Thông minh, Top cuối (ẩn/hiện).
    *   `qwerty_keyboard_layout`: Chứa các hàng phím chữ (ẩn/hiện).
2.  **Logic Layer**: `BotKeyboardService` kế thừa `onKey` hoặc xử lý click từ các phím XML.
