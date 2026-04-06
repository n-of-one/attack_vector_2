// --- Types ---

export type ShapeType = 'invected' | 'embattled' | 'indented' | 'raguly'
export type EdgeType = 'top' | 'right' | 'bottom' | 'left'

export interface ShapedEdge {
    shape: ShapeType
    dir: 'out' | 'in'
}

export interface FlatEdge {
    dir: 'flat'
}

export type EdgeConfig = ShapedEdge | FlatEdge

export interface PieceConfig {
    col: number
    row: number
    x: number
    y: number
    rotation: number // 0, 90, 180, or 270
    top: EdgeConfig
    right: EdgeConfig
    bottom: EdgeConfig
    left: EdgeConfig
}

export const FLAT: FlatEdge = {dir: 'flat'}

// Canonical point: [along, perp] relative to an edge
type CanonicalPoint = [number, number]
type ShapeGenerator = (size: number, tabHeight: number) => CanonicalPoint[]

const TAB_HEIGHT_RATIO = 0.08
const EDGE_MARGIN_RATIO = 0.15
const CANVAS_MARGIN = 80
const MAX_PIECE_SIZE = 150

const ROTATIONS = [0, 90, 180, 270]

export function calculatePieceSize(canvasWidth: number, canvasHeight: number, cols: number, rows: number): number {
    const maxPieceSize = Math.min(
        (canvasWidth - CANVAS_MARGIN) / (cols + 1),
        (canvasHeight - CANVAS_MARGIN) / (rows + 1)
    )
    return Math.ceil(Math.min(maxPieceSize, MAX_PIECE_SIZE))
}

// --- Shape definitions ---
// Each shape returns [along, perp] points in canonical form:
//   along = pixels along the edge from start corner
//   perp  = pixels outward from the piece (positive = away from center)

const SEGMENTS_PER_BULB = 8

export const SHAPES: Record<ShapeType, ShapeGenerator> = {
    // Consecutive semicircular bulbs (no gaps between bulbs)
    // Uses tabHeight as max perp so the extrusion matches the layout extension
    invected: (size, tabHeight) => {
        const margin = size * EDGE_MARGIN_RATIO
        const inner = size - 2 * margin
        const count = 4
        const rx = inner / (count * 2) // horizontal radius: fills the edge
        const ry = tabHeight            // vertical radius: matches extension
        const points: CanonicalPoint[] = []
        for (let i = 0; i < count; i++) {
            const center = margin + rx * (2 * i + 1)
            for (let j = 0; j <= SEGMENTS_PER_BULB; j++) {
                const angle = Math.PI - j * Math.PI / SEGMENTS_PER_BULB
                points.push([center + rx * Math.cos(angle), ry * Math.sin(angle)])
            }
        }
        return points
    },
    // Square battlements — starts and ends with a merlon
    embattled: (size, tabHeight) => {
        const margin = size * EDGE_MARGIN_RATIO
        const inner = size - 2 * margin
        const count = 3
        const unit = inner / (count * 2 - 1)
        const points: CanonicalPoint[] = []
        points.push([margin, 0])
        points.push([margin, tabHeight])
        for (let i = 0; i < count; i++) {
            const mEnd = margin + (2 * i + 1) * unit
            points.push([mEnd, tabHeight])
            if (i < count - 1) {
                points.push([mEnd, 0])
                const gEnd = margin + (2 * i + 2) * unit
                points.push([gEnd, 0])
                points.push([gEnd, tabHeight])
            }
        }
        points.push([size - margin, 0])
        return points
    },
    // Zigzag triangles
    indented: (size, tabHeight) => {
        const margin = size * EDGE_MARGIN_RATIO
        const inner = size - 2 * margin
        const count = 5
        const toothWidth = inner / count
        const points: CanonicalPoint[] = []
        points.push([margin, 0])
        for (let i = 0; i < count; i++) {
            points.push([margin + (i + 0.5) * toothWidth, tabHeight])
            points.push([margin + (i + 1) * toothWidth, 0])
        }
        return points
    },
    // Diagonal branch stumps — starts and ends with a stump, perpendicular at edges
    raguly: (size, tabHeight) => {
        const margin = size * EDGE_MARGIN_RATIO
        const inner = size - 2 * margin
        const count = 4
        const unit = inner / (count * 2 - 1)
        const slant = unit * 0.3
        const points: CanonicalPoint[] = []
        points.push([margin, 0])
        points.push([margin, tabHeight])
        for (let i = 0; i < count; i++) {
            const stumpEnd = margin + (2 * i + 1) * unit
            points.push([stumpEnd - slant / 2, tabHeight])
            if (i < count - 1) {
                points.push([stumpEnd + slant / 2, 0])
                const gapEnd = margin + (2 * i + 2) * unit
                points.push([gapEnd - slant / 2, 0])
                points.push([gapEnd + slant / 2, tabHeight])
            }
        }
        points.push([size - margin, tabHeight])
        points.push([size - margin, 0])
        return points
    },
}

const ALL_SHAPES: ShapeType[] = ['invected', 'embattled', 'indented', 'raguly']

// --- Edge mapper ---
// Map canonical (along, perp) to canvas (x, y).
// Positive perp points outward from the piece center.

type EdgeMapper = (along: number, perp: number) => [number, number]

export function getEdgeMapper(edge: EdgeType, originX: number, originY: number, size: number): EdgeMapper {
    switch (edge) {
        case 'top':
            return (along, perp) => [originX + along, originY - perp]
        case 'right':
            return (along, perp) => [originX + size + perp, originY + along]
        case 'bottom':
            return (along, perp) => [originX + size - along, originY + size + perp]
        case 'left':
            return (along, perp) => [originX - perp, originY + size - along]
    }
}

// --- Path builder ---

/** Round to 2 decimal places to eliminate floating-point drift between matching in/out edges. */
function round(n: number): number {
    return Math.round(n * 100) / 100
}

export function buildPiecePath(originX: number, originY: number, size: number, config: PieceConfig): string {
    const tabHeight = size * TAB_HEIGHT_RATIO
    let pathString = `M ${round(originX)} ${round(originY)}`

    const edges: Array<{ name: EdgeType, edgeConfig: EdgeConfig }> = [
        {name: 'top', edgeConfig: config.top},
        {name: 'right', edgeConfig: config.right},
        {name: 'bottom', edgeConfig: config.bottom},
        {name: 'left', edgeConfig: config.left},
    ]

    for (const {name, edgeConfig} of edges) {
        const mapper = getEdgeMapper(name, originX, originY, size)

        if (edgeConfig.dir === 'flat') {
            const [endX, endY] = mapper(size, 0)
            pathString += ` L ${round(endX)} ${round(endY)}`
        } else {
            const direction = edgeConfig.dir === 'out' ? 1 : -1
            const points = SHAPES[edgeConfig.shape](size, tabHeight)
            for (const [along, perp] of points) {
                const [x, y] = mapper(along, perp * direction)
                pathString += ` L ${round(x)} ${round(y)}`
            }
            const [endX, endY] = mapper(size, 0)
            pathString += ` L ${round(endX)} ${round(endY)}`
        }
    }

    pathString += ' Z'
    return pathString
}

/**
 * Build an SVG path string for just the flat (border) edges of a piece.
 * Returns null if the piece has no flat edges (center piece).
 * Includes invisible anchor points at the main path's bounding box corners
 * so that fabric.js computes the same pathOffset/dimensions as the main path.
 */
export function buildBorderPath(originX: number, originY: number, size: number, config: PieceConfig): string | null {
    const edges: Array<{ name: EdgeType, edgeConfig: EdgeConfig }> = [
        {name: 'top', edgeConfig: config.top},
        {name: 'right', edgeConfig: config.right},
        {name: 'bottom', edgeConfig: config.bottom},
        {name: 'left', edgeConfig: config.left},
    ]

    const hasBorder = edges.some(e => e.edgeConfig.dir === 'flat')
    if (!hasBorder) return null

    // Compute the full bounding box to match the main path.
    // The main path always starts at (0,0) min because extensions push the origin inward.
    const tabHeight = size * TAB_HEIGHT_RATIO
    const extRight = config.right.dir === 'out' ? tabHeight : 0
    const extBottom = config.bottom.dir === 'out' ? tabHeight : 0
    const maxX = round(originX + size + extRight)
    const maxY = round(originY + size + extBottom)

    // Anchor M commands at opposite corners to force same bounding box as main path
    let pathString = `M 0 0 M ${maxX} ${maxY}`

    for (const {name, edgeConfig} of edges) {
        if (edgeConfig.dir !== 'flat') continue
        const mapper = getEdgeMapper(name, originX, originY, size)
        const [startX, startY] = mapper(0, 0)
        const [endX, endY] = mapper(size, 0)
        pathString += ` M ${round(startX)} ${round(startY)} L ${round(endX)} ${round(endY)}`
    }

    return pathString
}

// --- Grid generation ---

export function flipDir(edge: EdgeConfig): EdgeConfig {
    if (edge.dir === 'flat') return FLAT
    return {shape: edge.shape, dir: edge.dir === 'out' ? 'in' : 'out'}
}

function randomShape(): ShapeType {
    return ALL_SHAPES[Math.floor(Math.random() * ALL_SHAPES.length)]
}

function randomDir(): 'out' | 'in' {
    return Math.random() < 0.5 ? 'out' : 'in'
}

// FIXME: fix the image aspect-ratio and find corresponding piece sizes
export function generatePieceConfigs(cols: number, rows: number, imageWidth: number, imageHeight: number,
                                     canvasWidth: number, canvasHeight: number): PieceConfig[] {
    const pieceSize = calculatePieceSize(imageWidth, imageHeight, cols, rows)
    const tabSize = pieceSize * TAB_HEIGHT_RATIO

    // Pre-generate internal edges
    // horizontalEdges[row][col] = edge between (col, row).right and (col+1, row).left
    const horizontalEdges: ShapedEdge[][] = []
    for (let row = 0; row < rows; row++) {
        horizontalEdges[row] = []
        for (let col = 0; col < cols - 1; col++) {
            horizontalEdges[row][col] = {shape: randomShape(), dir: randomDir()}
        }
    }

    // verticalEdges[row][col] = edge between (col, row).bottom and (col, row+1).top
    const verticalEdges: ShapedEdge[][] = []
    for (let row = 0; row < rows - 1; row++) {
        verticalEdges[row] = []
        for (let col = 0; col < cols; col++) {
            verticalEdges[row][col] = {shape: randomShape(), dir: randomDir()}
        }
    }

    const positions = generateSurroundingPositions(cols * rows, pieceSize, tabSize, cols, rows, canvasWidth, canvasHeight)

    const pieces: PieceConfig[] = []

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            const top: EdgeConfig = row === 0 ? FLAT : flipDir(verticalEdges[row - 1][col])
            const bottom: EdgeConfig = row === rows - 1 ? FLAT : verticalEdges[row][col]
            const left: EdgeConfig = col === 0 ? FLAT : flipDir(horizontalEdges[row][col - 1])
            const right: EdgeConfig = col === cols - 1 ? FLAT : horizontalEdges[row][col]

            const {x, y} = positions[row * cols + col]
            const rotation = ROTATIONS[Math.floor(Math.random() * 4)]

            pieces.push({col, row, x, y, rotation, top, right, bottom, left})
        }
    }

    return pieces
}

interface Rect {
    x: number
    y: number
    w: number
    h: number
}

/**
 * Generate positions for pieces distributed around the central puzzle image.
 * Defines 4 zones (left, right, top, bottom) around the centered puzzle area,
 * then distributes pieces across zones proportionally to zone area.
 * Pieces may partially overlap the puzzle image when space on a side is tight.
 */
function generateSurroundingPositions(
    count: number, pieceSize: number, tabSize: number,
    cols: number, rows: number,
    canvasWidth: number, canvasHeight: number
): Array<{ x: number, y: number }> {

    const puzzleWidth = pieceSize * cols
    const puzzleHeight = pieceSize * rows

    // The puzzle image is centered on the canvas
    const puzzleCenterX = canvasWidth / 2
    const puzzleCenterY = canvasHeight / 2

    // Puzzle bounds (no extra padding — partial overlap with the image is acceptable)
    const puzzleLeft = puzzleCenterX - puzzleWidth / 2
    const puzzleRight = puzzleCenterX + puzzleWidth / 2
    const puzzleTop = puzzleCenterY - puzzleHeight / 2
    const puzzleBottom = puzzleCenterY + puzzleHeight / 2

    // Piece placement margin from canvas edges
    const edgeMargin = tabSize

    // Define 4 zones around the puzzle area.
    // Left and right zones span full canvas height; top and bottom fill the gap between them.
    // Use Math.max to ensure zones always have at least some minimum width/height
    // so that every side gets pieces even when space is tight (partial overlap is OK).
    const minZoneSize = pieceSize * 0.5
    const zones: Rect[] = [
        // Left
        {
            x: edgeMargin, y: edgeMargin,
            w: Math.max(minZoneSize, puzzleLeft - edgeMargin),
            h: canvasHeight - edgeMargin * 2
        },
        // Right
        {
            x: Math.min(puzzleRight, canvasWidth - edgeMargin - minZoneSize), y: edgeMargin,
            w: Math.max(minZoneSize, canvasWidth - edgeMargin - puzzleRight),
            h: canvasHeight - edgeMargin * 2
        },
        // Top (between left and right zones)
        {
            x: puzzleLeft, y: edgeMargin,
            w: Math.max(minZoneSize, puzzleRight - puzzleLeft),
            h: Math.max(minZoneSize, puzzleTop - edgeMargin)
        },
        // Bottom (between left and right zones)
        {
            x: puzzleLeft, y: Math.min(puzzleBottom, canvasHeight - edgeMargin - minZoneSize),
            w: Math.max(minZoneSize, puzzleRight - puzzleLeft),
            h: Math.max(minZoneSize, canvasHeight - edgeMargin - puzzleBottom)
        },
    ]

    // Distribute pieces across zones proportionally to zone area
    const totalArea = zones.reduce((sum, z) => sum + z.w * z.h, 0)
    const positions: Array<{ x: number, y: number }> = []

    let remaining = count
    for (let i = 0; i < zones.length; i++) {
        const zone = zones[i]
        const zonePieceCount = (i === zones.length - 1)
            ? remaining  // last zone gets whatever is left
            : Math.round(count * (zone.w * zone.h) / totalArea)
        remaining -= zonePieceCount

        // Determine grid dimensions to spread pieces evenly across the zone.
        // Choose grid rows/cols to match the zone's aspect ratio while fitting zonePieceCount cells.
        const zoneAspect = zone.w / zone.h
        const gridRows = Math.max(1, Math.round(Math.sqrt(zonePieceCount / zoneAspect)))
        const gridCols = Math.max(1, Math.ceil(zonePieceCount / gridRows))

        // Spacing: spread cells evenly across the full zone
        const spacingX = gridCols > 1 ? zone.w / gridCols : 0
        const spacingY = gridRows > 1 ? zone.h / gridRows : 0

        // Small jitter so pieces don't look perfectly aligned
        const jitterX = spacingX * 0.15
        const jitterY = spacingY * 0.15

        for (let j = 0; j < zonePieceCount; j++) {
            const gridCol = j % gridCols
            const gridRow = Math.floor(j / gridCols)
            // Center each piece within its cell
            const baseX = zone.x + (gridCol + 0.5) * spacingX
            const baseY = zone.y + (gridRow + 0.5) * spacingY
            positions.push({
                x: baseX + (Math.random() - 0.5) * 2 * jitterX,
                y: baseY + (Math.random() - 0.5) * 2 * jitterY,
            })
        }
    }

    // Shuffle so pieces from the same grid row aren't all in the same zone
    for (let i = positions.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [positions[i], positions[j]] = [positions[j], positions[i]]
    }

    // Clamp all positions so pieces stay fully on canvas.
    // With center-origin, the worst-case half-extent is (pieceSize + 2*tabSize) / 2 (out-tabs on both sides).
    const halfExtent = (pieceSize + 2 * tabSize) / 2
    for (const pos of positions) {
        pos.x = Math.max(halfExtent, Math.min(canvasWidth - halfExtent, pos.x))
        pos.y = Math.max(halfExtent, Math.min(canvasHeight - halfExtent, pos.y))
    }

    return positions
}
