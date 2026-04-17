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
- The image source path is provided by the server in the enter data (e.g. `/img/frontier/ice/jigsaw/green.png`). Videos (`.mp4`) are also supported.
- The server picks a random source from a per-strength bucket (`JigsawDefaultMedia.kt`) at ICE creation time, so every hacker entering the same ICE sees the
  same media.
- In testing mode (`DEV_TESTING_MODE`), the creator uses a deterministic RNG seed (0) so puzzles are reproducible.

## Canvas

- Canvas size: 1880 x 928 pixels.
- The puzzle display area is centered on the canvas.
- A pixelated (low-resolution) version of the source image is shown as a semi-transparent background (opacity 0.15) at the puzzle position, as a visual guide
  for the player. The pixelation is achieved by first downsampling the image to roughly 144 x 80 pixels (scaled by aspect ratio) and then scaling it back up
  without smoothing, giving a blocky look.

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

- Each piece displays the corresponding section of the source image, clipped to the piece's shape (including tabs and recesses).
- Each piece has a thin outline ("stroke") drawn on top, in color `#88aacc` with a width of 1px. The stroke opacity fades as more neighbors snap to the piece
  (see Snap section).
- Pieces on the outer border of the puzzle additionally have a white border, 1.5px wide, drawn only along their flat (border) edges. Center pieces have no
  such border.
- Clicks/hits on a piece only register on the actual piece shape, not on its bounding rectangle.

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
- While dragging, the snap group is rendered on top of all other pieces. Once the drag ends it returns to the normal rendering order.

### Rotation

- **Right-click** on a piece/group rotates it 90 degrees clockwise.
- **Mouse wheel** rotates: scroll down = clockwise, scroll up = counter-clockwise.
- Rotation is animated linearly over 200ms.
- Only one rotation can be in progress at a time (subsequent rotation inputs are ignored until the current animation completes).
- Rotation is always in 90-degree increments. Valid values: 0, 90, 180, 270.
- Rotation must be around the body center of the pieces that make the snap group.

### Context menu and page scroll

- The browser context menu is suppressed on the canvas (so right-click can be used for rotation).
- Browser wheel scroll is suppressed on the canvas (so the mouse wheel can be used for rotation).

### Selection

- There is no rubber-band or multi-select; pieces are interacted with one snap group at a time.

## Snap Groups

Pieces are organized into snap groups. A snap group is a set of one or more pieces that move and rotate together. Group state is server-authoritative (see
Server Communication § Snap engine). The client only:

- Sends `moved` / `rotate` commands when the user finishes a drag or rotation.
- Applies server broadcasts (`MOVED`, `ROTATE`, `SNAP`) to its local group display.

### Server-authoritative groups

- All snap-group state (position, rotation, membership) is owned by the server. The frontend never generates piece positions or rotations for pre-existing
  groups; it just renders what it receives.
- On enter, the server sends every piece as its own single-piece group plus whatever merged groups already exist from prior hacker activity (`JigsawGroup`
  objects with `id`, `pieces: [{column, row}]`, `rotation`, `x`, `y`).
- `(x, y)` on a group is the pivot in canvas coordinates: the centroid of the group pieces' body centers in grid-rotated space. Each piece's world body center =
  `pivot + rotate(gridOffsetFromGridCentroid, rotation)`.

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
- The body center is derived from the piece's own position and rotation combined with its snap group's position and rotation.
- Body centers are used for snap detection and positioning.
- Body centers are used for rotation.

## Data Model

`PieceConfig` describes immutable piece geometry only. Position and rotation live on the owning
`JigsawGroup`, not on the piece.

```typescript
interface PieceConfig {
    column: number        // Grid column (0-based)
    row: number           // Grid row (0-based)
    top: EdgeConfig       // Top edge shape
    right: EdgeConfig     // Right edge shape
    bottom: EdgeConfig    // Bottom edge shape
    left: EdgeConfig      // Left edge shape
}

interface EdgeConfig {
    shape: ShapeType | null   // 'invected' | 'embattled' | 'indented' | 'raguly'; null for flat
    direction: 'out' | 'in' | 'flat'
}

interface JigsawGroup {
    id: string                               // e.g. "g-0"; survives merges as the smallest id
    pieces: Array<{ column: number, row: number }>
    rotation: number                         // 0, 90, 180, 270
    x: number                                // pivot x in canvas coords
    y: number                                // pivot y in canvas coords
}
```

## Server Communication

The server is authoritative for all group state. The client never mutates groups locally; it
sends intents to the server and re-renders from the resulting broadcasts. Every command produces
exactly one broadcast: `MOVED`, `ROTATE`, or `SNAP`.

### Enter flow

1. Frontend connects via WebSocket and subscribes to `/topic/ice/{iceId}`.
2. Frontend sends `/ice/jigsaw/enter` with `{iceId}`.
3. Server responds with `SERVER_JIGSAW_ENTER` containing `JigsawEnterData`:
    - `hacked: boolean` - Whether the puzzle is already solved.
    - `strength: IceStrength` - Difficulty level.
   - `imageSource: string` - Path to the puzzle image/video.
    - `columns: number` - Grid columns.
    - `rows: number` - Grid rows.
   - `pieces: PieceConfig[]` - Edge geometry for every piece.
   - `groups: JigsawGroup[]` - Current snap groups with live pivot positions and rotations.

### Client → server commands

- `/ice/jigsaw/moved` — `{ iceId, groupId, x, y }`. Fired when the user finishes dragging a group.
- `/ice/jigsaw/rotate` — `{ iceId, groupId, rotation }`. Fired when the user rotates a group; rotation is the new absolute value (0/90/180/270).

Commands for a `groupId` that no longer exists (already absorbed by another hacker's snap) are dropped silently.

### Server → clients broadcasts (`/topic/ice/{iceId}`)

- `SERVER_JIGSAW_MOVED` — `{ groupId, x, y }`. The move didn't trigger a snap; update that group's pivot.
- `SERVER_JIGSAW_ROTATE` — `{ groupId, rotation }`. The rotation didn't trigger a snap; update that group's rotation.
- `SERVER_JIGSAW_SNAP` — `{ survivingGroupId, absorbedGroupIds, pieces, rotation, x, y }`. One or more groups snapped into the surviving group. `pieces` is the
  full piece membership of the surviving group; `absorbedGroupIds` is the complete list of groups removed in this settle pass (including transitive/bridge
  snaps).
- `SERVER_ICE_HACKED` — Puzzle solved. The frontend plays the hacked UI and the run progresses after a 4-second delay on the backend.
- `SERVER_RESET_ICE` — Triggers `processIceReset()` on the ice manager, which closes the connection.

### Snap engine (server)

After each `moved` or `rotate`, the server runs a settle loop seeded on the changed group:

1. Find every other group whose rotation matches the seed and whose grid-neighbour piece lies within **15 px** Chebyshev distance of its expected body-center
   position.
2. Pick the closest candidate's positional correction and apply it to the seed (so the merged geometry lands on exact grid positions).
3. Merge the seed with every in-tolerance neighbour in a single pass (bridge snaps collapse multiple groups at once).
4. The merged group keeps the smallest `id` (string compare) of the participants, and its pivot becomes the centroid of the merged pieces' world body centers.
5. Repeat with the merged group as the new seed until no more snaps trigger.

If no merges happened the server sends `MOVED` or `ROTATE`; otherwise it sends a single `SNAP` with the final surviving group state.

### Completion

- Puzzle is solved when `groups.size == 1 && groups[0].rotation == 0`.
- The server then marks the ICE hacked and schedules `SERVER_ICE_HACKED` via `HackedUtil` with a 4-second delay (`IceHackState.HACKED`).
- Further `moved` / `rotate` commands on a hacked ICE are ignored.

### Persistence

- `JigsawIceStatus` is stored in MongoDB (one document per ICE layer, indexed by `layerId`).
- `JigsawService.findOrCreateIceByLayerId` creates the status on first access; `resetIceByLayerId` deletes it and broadcasts `SERVER_RESET_ICE`.

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

- **Hacked state handling**: `enterHacked()` is called when `data.hacked` is true, but the jigsaw-specific completion UI is not implemented.
- **Terminal intro sequence**: A cinematic intro sequence with timed messages is commented out (connecting, analyzing, pattern fragmentation, exploit success,
  puzzle online). Currently skipped - puzzle shows immediately.

## Implementation Notes

PixiJS is used instead of Fabric.js to support movies as well as static images.

This section captures choices specific to PixiJS v8. They are implementation details, not functional requirements.

### Scene structure

- One PixiJS `Application` hosts the canvas. The stage is organized into three layers:
    1. **Background layer** - holds the pixelated guide image.
    2. **Pieces layer** - holds all snap groups under normal conditions.
    3. **Top layer** - the currently dragged snap group is temporarily reparented here so it renders above all other pieces.
- One shared `Texture` is used by all piece sprites (supports both image and video sources; video textures auto-update each frame).

### Piece composition

- Each piece is a `Container` holding:
    - A `Sprite` of the shared texture, clipped by a polygon `Graphics` mask in the shape of the piece.
    - A `Graphics` stroke overlay for the outline.
    - An optional `Graphics` border overlay for the white edge on border pieces.
- The container's polygon `hitArea` is set so pointer events only hit the actual shape.

### Input

- Pointer and wheel input is handled via Pixi's federated event system (`pointerdown` / `pointermove` / `pointerup` / `pointerupoutside` / `wheel`) on the
  stage.
- The stage uses `eventMode = 'static'` with a rectangular `hitArea` covering the full canvas so events reach it even when no piece is under the cursor.
- The browser `contextmenu` and `wheel` DOM events on the canvas element are prevented.

### Geometry

- Piece shape geometry is expressed as arrays of polygon vertices (not SVG paths). The same points feed the mask, stroke, hit area, and border overlay.
- Precomputed sin/cos values for 0/90/180/270 degrees avoid floating-point drift.
- All vertex coordinates are rounded to 2 decimal places so matching in/out edges share identical points.
- Body-center computation derives from local and parent transforms directly rather than relying on Pixi's cached world transform, which may not be refreshed
  during a single input tick.

### Animation

- Rotation animation is driven by a callback on the `Application.ticker` using linear interpolation. The callback unregisters itself on completion.

### Performance

- The ticker is capped at 30 FPS to reduce load on weaker machines. Pointer input still updates positions every event; only rendering runs less often.
- Antialiasing on the `Application` is disabled to save GPU fragment shader work; the piece outlines give sufficient edge definition.
