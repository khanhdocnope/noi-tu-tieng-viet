## 1. Core Logic Updates

- [x] 1.1 Add `Bottom` to `Mode` enum in `main.rs`.
- [x] 1.2 Update `suggest_next` in `PhraseIndex` to return all candidates or handle sorting. *Currently it truncates to `top_n` early. It should probably return all and let the caller decide or take a mode parameter.*
- [x] 1.3 Implement `pick_two_word` logic for `Mode::Bottom`: sort candidates by `next_count` descending and pick.
- [x] 1.4 Update `suggest_from_first` to support descending sort or implement it in `pick_one_word`.

## 2. Clipboard Command Integration

- [x] 2.1 Update the clipboard polling loop to detect `%!` command.
- [x] 2.2 Update state and status message when `%!` is detected.

## 3. UI and Polish

- [x] 3.1 Update `App::update` to show "🔥 BOTTOM TIER" when in `Mode::Bottom`.
- [x] 3.2 Add instructions for `%!` in the UI help labels.
- [x] 3.3 Verify behavior by copying `%!` and then a word.
