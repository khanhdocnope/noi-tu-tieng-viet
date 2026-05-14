## Context

The `clipboard_autosuggest` tool currently supports `Top` (highest priority) and `Smart` (random mix) modes. It lacks a way to explicitly select "worst" or "bottom-tier" suggestions, which can be useful for certain game strategies or testing.

## Goals / Non-Goals

**Goals:**
- Implement a `Bottom` mode that picks words with the highest number of next branches (hardest to "close").
- Add a clipboard command `%! ` to toggle this mode.
- Update the GUI to reflect the new mode.

**Non-Goals:**
- Changing the dictionary format.
- Adding complex multi-step strategy logic.

## Decisions

- **Enum Update**: Add `Mode::Bottom` to the `Mode` enum.
- **Picking Logic**: 
  - For `suggest_from_first`: Sort the ranked list by `next_count` in **descending** order instead of ascending when in `Bottom` mode.
  - For `suggest_next`: Currently, `suggest_next` only returns the first `top_n` phrases from the file order. To implement `Bottom` mode correctly, we should rank these as well, or at least take from the end of the `indices` list if we assume file order has some relevance (though sorting by `next_count` is more robust).
- **Clipboard Command**: Recognize `current_norm == "%!"` as a command.
- **UI**: Add a specific color (maybe orange or yellow) for the `Bottom` mode indicator.

## Risks / Trade-offs

- **Performance**: Sorting the entire list of possible words for a prefix might be slightly slower if the list is huge, but `suggest_from_first` already limits to 200 before sorting.
- **Usability**: `%! ` is a specific command that needs to be communicated to the user (adding to the help labels in the UI).
