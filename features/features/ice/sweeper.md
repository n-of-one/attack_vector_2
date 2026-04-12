# Sweeper ICE

Sweeper ICE is a minesweeper variant. The hacker must identify all mines in a grid without detonating them.

## Capabilities
- Grid of hidden cells containing mines or numbers (indicating adjacent mine count)
- Left-click reveals a cell; right-click flags it as a mine
- Revealing a mine locks the hacker out of this ICE permanently (other hackers can continue)
- Revealing a zero-cell cascades to reveal all adjacent empty cells
- Flagged cells cannot be accidentally revealed
- Win condition: all non-mine cells revealed
- Grid size and mine count scale with ICE strength (9x9 with 8 mines at Very Weak up to 32x18 with 120 mines at Onyx)
- Top-left cell is guaranteed to be empty (safe starting point)
- Multiple hackers can collaborate on the same Sweeper simultaneously
- Multi-stage reset: start reset, then hold to complete (generates a new board)
- Cannot reset an already-solved Sweeper
- Blocked hackers can be unblocked via the SWEEPER_UNBLOCK script effect
- In testing mode, the board is seeded for reproducible layouts
