## ADDED Requirements

### Requirement: Bottom Suggestion Mode Command
The system must recognize the clipboard content `%! ` (case-insensitive, trimmed) as a command to switch the bot to "Bottom Suggestion" mode.

#### Scenario: Switching to Bottom Mode
- **WHEN** the user copies `%! ` to the clipboard.
- **THEN** the bot's internal state must change to `Mode::Bottom`.
- **AND** the UI status must display "✅ Đã chuyển sang chế độ BOTTOM TIER (Top cuối)".
- **AND** the mode indicator must show "🔥 BOTTOM TIER (Top cuối)".

### Requirement: Bottom Suggestion Selection Logic
In "Bottom Suggestion" mode, the bot must select words that have the highest number of possible next moves (highest branching factor).

#### Scenario: Picking from 1-word input in Bottom Mode
- **WHEN** the user copies a single word (e.g., "anh").
- **THEN** the bot must find all valid phrases starting with "anh".
- **AND** sort them by `next_count` in **descending** order.
- **AND** pick one from the top of this sorted list (highest `next_count`).

#### Scenario: Picking from 2-word input in Bottom Mode
- **WHEN** the user copies a phrase (e.g., "xin chào").
- **THEN** the bot must find all valid phrases starting with "chào".
- **AND** sort them by `next_count` in **descending** order.
- **AND** pick one from the top of this sorted list.
