## Why

The user wants more control over word selection in the Vietnamese Word Chain (Nối Từ) bot. Specifically, they want a way to force the bot to pick "bottom-tier" words (words with many next options or simply those at the end of the suggestion list) for strategic reasons or testing. Currently, the bot favors "top-tier" words (least branching) or smart random.

## What Changes

Introduce a new command `%! ` which, when copied to the clipboard, switches the bot into a "Bottom Suggestion" mode. In this mode, the bot will pick suggestions from the end of the ranked list (highest `next_count`) instead of the beginning.

## Capabilities

### New Capabilities
- `bottom-suggestion-mode`: Ability to trigger and use a suggestion logic that picks words from the end of the ranked list.

### Modified Capabilities
- `clipboard-monitor`: Update the clipboard listener to recognize the `%! ` command.

## Impact

- `clipboard_autosuggest/src/main.rs`: Logic for command parsing and picking suggestions will be modified.
- `AppState`: New mode `Bottom` will be added to the state.
- UI: The current mode indicator will be updated to show "BOTTOM MODE".
