## Context

Dự án "Bot Nối Từ Việt" là một giải pháp đa nền tảng (PC & Mobile) giúp người chơi tự động tìm và điền từ trong trò chơi Nối Từ. Dự án đã có mã nguồn hoạt động tốt cho cả hai nền tảng nhưng thiếu một bản thiết kế hệ thống tập trung.

## Goals / Non-Goals

**Goals:**
- Hệ thống lại kiến trúc đa ngôn ngữ (Rust, Java).
- Làm rõ cơ chế giao tiếp giữa ứng dụng và dữ liệu từ điển (`TuVung.txt`).
- Mô tả quy trình build tự động (CI/CD) cho Android.

**Non-Goals:**
- Thay đổi code chức năng hiện có.
- Thêm tính năng mới (chỉ tập trung vào tài liệu hóa).

## Decisions

- **Cấu trúc dữ liệu**: Sử dụng file văn bản phẳng (`TuVung.txt`) để lưu trữ từ điển giúp việc truy xuất nhanh và dễ chỉnh sửa.
- **Phiên bản PC**: Sử dụng Rust để đảm bảo hiệu suất tối đa và chiếm dụng RAM cực thấp (~2MB).
- **Phiên bản Android**: Xây dựng dưới dạng một Input Method Editor (IME) - Bàn phím ảo để có thể can thiệp trực tiếp vào ô nhập liệu của các ứng dụng khác mà không cần quyền Accessibility phức tạp.
- **Giao diện**: Áp dụng phong cách "Gaming Dark" đồng nhất trên cả hai nền tảng.

## Risks / Trade-offs

- **Dữ liệu lớn**: File `TuVung.txt` hơn 53k từ có thể gây lag nhẹ khi load lần đầu trên các thiết bị Android cũ.
- **Bảo mật**: Việc gõ tự động trên Android cần người dùng cấp quyền Bàn phím, cần có hướng dẫn rõ ràng để người dùng tin tưởng.
