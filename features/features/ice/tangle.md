# Tangle ICE

Tangle ICE presents a visual puzzle where the hacker must drag points to untangle all crossing lines.

## Capabilities
- Points connected by line segments are arranged on a 2D canvas
- Drag points to new positions to eliminate line crossings
- Win condition: no line segments intersect (except at shared endpoints)
- Points are grouped into clusters; cluster count and point count scale with ICE strength (10 points at Very Weak up to 180 points at Onyx)
- Clusters are hidden by default; only revealed at Weak strength or via TANGLE_REVEAL_CLUSTERS script effect
- Points start arranged in a circle and must be rearranged
- Intersection detection runs server-side after each move
- Points are constrained to the canvas boundaries (10px padding)
- In testing mode, puzzle generation is seeded for reproducibility
- New puzzle generated on reset
