# Netwalk ICE

Netwalk ICE presents a grid-based routing puzzle. The hacker must rotate tiles to connect all segments into a single continuous path.

## Capabilities
- Grid of cells, each containing a line segment with up to 4 directions (N, E, S, W)
- Click a cell to rotate it clockwise
- Win condition: all cells connected into a single continuous path
- Grid size scales with ICE strength (7x7 at Very Weak up to 17x11 at Onyx)
- Very Strong and Onyx enable wrapping (edges connect to opposite sides, making the grid toroidal)
- Connection state is validated server-side after each rotation
- Winning is detected automatically after each move
- No hints, no timeouts, no progressive difficulty
- New puzzle generated on reset
