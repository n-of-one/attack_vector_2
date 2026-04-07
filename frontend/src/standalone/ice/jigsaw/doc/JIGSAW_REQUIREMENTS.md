# Jigsaw Puzzle ICE - Requirements

## Overview

Jigsaw is an ICE type in Attack Vector. Players must assemble a jigsaw puzzle to hack through the ICE layer. The puzzle uses a source image that is cut into
pieces with decorative non-interlocking edge shapes. Pieces start scattered around the puzzle area, randomly rotated, and must be dragged, rotated, and snapped
together to reassemble the image.

The ICE theme name is **Paheli** (Hindi for "puzzle").

## Difficulty / Strength

ICE strength determines the number of puzzle pieces via a grid of columns x rows:

| Strength    | Columns | Rows | Pieces |
|-------------|---------|------|--------|
| VERY_WEAK   | 5       | 3    | 15     |
| WEAK        | 7       | 4    | 28     |
| AVERAGE     | 10      | 6    | 60     |
| STRONG      | 13      | 8    | 104    |
| VERY_STRONG | 17      | 10   | 170    |
| ONYX        | 21      | 12   | 252    |

## Puzzle Image

- The source image must be at least 1376 x 768 pixels.
- The image is center-cropped to exactly 1376 x 768 from the source.
- The image is displayed at 80% scale (PUZZLE_SCALE = 0.8), giving a display size of 1100.8 x 614.4 pixels.
- The image source path is provided by the server in the enter data (e.g. `/img/frontier/ice/jigsaw/anubis.png`).

## Canvas

- Canvas size: 1880 x 928 pixels.
- The puzzle display area is centered on the canvas.
- A pixelated (low-resolution) version of the source image is shown as a semi-transparent background (opacity 0.25) at the puzzle position, as a visual guide
  for the player.
    - Created by drawing the image at ~144 x 80 pixels (96 * 1.5 scaled by aspect ratio), then scaling back up with `imageSmoothingEnabled = false`.

## Piece Shapes

Each piece is a rectangle with decorative edges. There are 4 edge shape types, inspired by heraldic line styles:

1. **Invected** - Consecutive semicircular bulbs (4 bulbs per edge). Smooth rounded extrusions using elliptical arcs.
2. **Embattled** - Square battlements (castle-wall pattern). 3 rectangular merlons with gaps between them.
3. **Indented** - Zigzag triangles (sawtooth pattern). 5 triangular teeth per edge.
4. **Raguly** - Diagonal branch stumps. 4 stumps with slanted transitions (30% of stump height).

### Edge geometry:

- Tab height (the perpendicular extrusion size) = edge length * 0.08 (TAB_HEIGHT_RATIO).
- Flat margin at start/end of each edge = edge length * 0.15 (EDGE_MARGIN_RATIO). Shapes only occupy the inner 70% of the edge.
- Edges are defined as canonical `[along, perp]` points that are then mapped to canvas coordinates based on which side of the piece (top/right/bottom/left) the
  edge belongs to.
- All coordinates are rounded to 2 decimal places to eliminate floating-point drift between matching in/out edges.

The jigsaw shapes are detailed in the file: `jigsaw-shapes.html`.

### Edge rules

- Border edges (edges on the outer perimeter of the puzzle) are always **flat** (straight line).
- Internal edges are randomly assigned one of the 4 shape types.
- Each internal edge has a random direction: **out** (protruding) or **in** (recessed).
- Adjacent pieces have complementary edges: if piece A's right edge is `{shape: embattled, dir: out}`, then piece B's left edge is
  `{shape: embattled, dir: in}`.

### Piece rendering

- Each piece is rendered as a fabric.js Group containing:
    - A **main path** (SVG path) filled with the corresponding section of the source image via a pattern fill.
    - An optional **border path** (white stroke, 1.5px) drawn only along the flat (border) edges of edge/corner pieces. Center pieces have no border path.
- Stroke color: `#88aacc`, width 1px, with opacity that fades as more neighbors snap (see Snap section).
- The border path includes invisible anchor points at the bounding box corners of the main path, so fabric.js computes identical dimensions for both paths.

## Initial Piece Placement

Pieces start scattered in 4 zones around the central puzzle image:

1. **Left zone** - Full canvas height, left of the puzzle area.
2. **Right zone** - Full canvas height, right of the puzzle area.
3. **Top zone** - Above the puzzle area, between left and right zones.
4. **Bottom zone** - Below the puzzle area, between left and right zones.

### Distribution rules

- Pieces are distributed across zones proportionally to zone area.
- Within each zone, pieces are arranged in a grid matching the zone's aspect ratio, with random jitter (+-15% of cell spacing) so they don't look perfectly
  aligned.
- The piece list is shuffled after zone assignment so pieces from the same puzzle row aren't clustered in the same zone.
- Positions are clamped so pieces stay fully on canvas (using halfExtent padding derived from maximum piece dimension + tab size).
- Zones are pushed away from the puzzle image by halfExtent when space permits, to avoid overlap. When space is tight, zones may partially overlap the puzzle
  image.

### Initial rotation

- Each piece starts with a random rotation: 0, 90, 180, or 270 degrees.

## Interaction

### Drag

- Left-click and drag moves the entire snap group (the piece and any pieces already snapped to it).
- On mouse-up (left button), the group is brought to the front of the canvas z-order.

### Rotation

- **Right-click** on a piece/group rotates it 90 degrees clockwise.
- **Mouse wheel** rotates: scroll down = clockwise, scroll up = counter-clockwise.
- Rotation is animated over 200ms.
- Only one rotation can be in progress at a time (subsequent rotation inputs are ignored until the current animation completes).
- Rotation is always in 90-degree increments. Valid values: 0, 90, 180, 270.
- Rotation must be around the body center of the pieces that make the snap group.

### Context menu

- The browser context menu is suppressed on the canvas (`stopContextMenu: true`).

### Selection

- Native fabric.js multi-select (rubber band selection) is disabled (`selection: false`).
- Pieces have no resize handles or borders (`hasControls: false`, `hasBorders: false`).
- Hover cursor is `grab`.
- Per-pixel target finding is enabled (`perPixelTargetFind: true`) so clicks must land on the actual piece shape, not its bounding box.

## Snap Groups

Pieces are organized into snap groups. A snap group is a set of one or more pieces that move and rotate together.

### Snap detection

- After a drag completes (`object:modified` event), the system checks all pieces in the moved group for potential snaps to neighboring pieces.
- A neighbor is a piece at an adjacent grid position (left, right, above, below).
- Snap criteria:
    1. The neighbor must have the same rotation as the dragged piece.
    2. The neighbor must not already be in the same snap group.
    3. The distance between the neighbor's actual body center and its expected body center (computed from the dragged piece's position + grid offset, rotated)
       must be less than **15 pixels** (Chebyshev distance: `max(|dx|, |dy|) < 15`).
- The closest valid match is chosen.

### Group merging

- When a snap occurs, the two snap groups are merged.

### Pre-existing groups

- The server can send pre-snapped groups in the enter data. Each group is a list of `[col, row]` pairs.
- The first piece in a group is the anchor. Other pieces are positioned relative to the anchor, matching its rotation.

## Stroke Opacity Feedback

The stroke (outline) of each piece fades as more of its neighbors are snapped to it, providing visual feedback of progress:

| Max neighbors | 0 snapped | 1 snapped | 2 snapped | 3 snapped | 4 snapped |
|---------------|-----------|-----------|-----------|-----------|-----------|
| 2 (corner)    | 100%      | 60%       | 10%       | -         | -         |
| 3 (edge)      | 100%      | 60%       | 40%       | 10%       | -         |
| 4 (center)    | 100%      | 60%       | 40%       | 30%       | 10%       |

Opacity is updated after every snap event for all pieces in the merged group.

The optional border path (of pieces that are on the edge) is not adjusted this way.

## Body Center Calculation

Each piece tracks a "body center" - the center of the rectangular piece body, ignoring tab extensions. This is distinct from the bounding-box center because
outward tabs extend the bounding box asymmetrically.

- `bodyOffsetX = (extensionLeft - extensionRight) / 2`
- `bodyOffsetY = (extensionTop - extensionBottom) / 2`
- The body center is computed from the fabric.js transform matrix, accounting for parent group transforms and rotation.
- Body centers are used for snap detection and positioning.
- Body centers are used for rotation

## Piece Configuration Data Model

Each piece is described by a `PieceConfig`:

```typescript
interface PieceConfig {
    col: number           // Grid column (0-based)
    row: number           // Grid row (0-based)
    x: number             // Initial canvas x position
    y: number             // Initial canvas y position
    rotation: number      // Initial rotation: 0, 90, 180, or 270
    top: EdgeConfig       // Top edge shape
    right: EdgeConfig     // Right edge shape
    bottom: EdgeConfig    // Bottom edge shape
    left: EdgeConfig      // Left edge shape
}

type EdgeConfig = ShapedEdge | FlatEdge

interface ShapedEdge {
    shape: ShapeType       // 'invected' | 'embattled' | 'indented' | 'raguly'
    dir: 'out' | 'in'
}

interface FlatEdge {
    dir: 'flat'
}
```

## Server Communication

### Enter flow

1. Frontend connects via WebSocket and subscribes to `/topic/ice/{iceId}`.
2. Frontend sends `/ice/jigsaw/enter` with `{iceId}`.
3. Server responds with `SERVER_JIGSAW_ENTER` containing `JigsawEnterData`:
    - `hacked: boolean` - Whether the puzzle is already solved.
    - `strength: IceStrength` - Difficulty level.
    - `imageSrc: string` - Path to the puzzle image.
    - `columns: number` - Grid columns.
    - `rows: number` - Grid rows.
    - `pieces: PieceConfig[]` - Configuration for every piece.
    - `groups: PieceGroup[]` - Pre-snapped groups (each is a list of `[col, row]` pairs).

### Completion

- `SERVER_ICE_HACKED` action is defined but not yet implemented.
- The `solved` flag exists on the ice manager but completion detection is not yet wired up.

### Reset

- `SERVER_RESET_ICE` triggers `processIceReset()` on the ice manager, which closes the connection.

## UI Layout

- Top bar contains (left to right):
    - Hacker presence indicator (col-lg-2, max 80px height, scrollable).
    - ICE title "Paheli" with strength indicator (col-lg-2).
    - Terminal display for status messages (col-lg-7, height 84px, font size 12).
    - Close tab button (col-lg-1, right-aligned).
- Below: the canvas element, with 3px border radius, 10px top/bottom margin.
- The canvas fades in via CSS transition (`transition_alpha_fast`) when the UI mode changes from HIDDEN to VISIBLE.

## State Management

Redux store structure:

```typescript
interface JigsawRootState {
    currentPage: Page
    hackers: IceHackers
    ui: JigsawUiState
    displayTerminal: TerminalState
}

interface JigsawUiState {
    strength: IceStrength
    mode: UiMode   // HIDDEN | VISIBLE
}
```

- On enter: strength is set, mode stays HIDDEN.
- On begin: mode transitions to VISIBLE, canvas becomes visible.
- Terminal shows "Puzzle interface online" immediately.

## Multiplayer

- WebSocket-based real-time updates via `/topic/ice/{iceId}`.
- `IceHackerPresence` component shows active hackers working on the puzzle.
- Shared puzzle state across multiple hackers (groups sent from server).

## Not Yet Implemented

- **Completion detection**: No logic to detect when all pieces are in a single snap group at correct positions and rotation 0.
- **Server-side puzzle logic**: The backend directory exists but is empty. Currently the frontend generates all piece configs, positions, and shapes locally (
  simulating the server response).
- **Hacked state handling**: `enterHacked()` is called when `data.hacked` is true, but the jigsaw-specific completion UI is not implemented.
- **Terminal intro sequence**: A cinematic intro sequence with timed messages is commented out (connecting, analyzing, pattern fragmentation, exploit success,
  puzzle online). Currently skipped - puzzle shows immediately.

## Technical Stack

- **Fabric.js** v5.5.2 for canvas rendering and interaction.
- **React + Redux** for UI state management.
- **TypeScript** throughout.
- SVG path strings for piece shape geometry (M/L commands only, no curves).
- Center-origin mode for all fabric objects (`originX/Y = "center"`).
- Precomputed trigonometry for 0/90/180/270 rotation to avoid floating-point drift.
