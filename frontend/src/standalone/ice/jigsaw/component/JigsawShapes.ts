// --- Types ---

export type ShapeType = 'invected' | 'embattled' | 'indented' | 'raguly'
export type EdgeType = 'top' | 'right' | 'bottom' | 'left'

/**
 * Flat edge-structure used on the wire (matches backend EdgeConfig exactly).
 *  - flat (border):  {shape: null, direction: 'flat'}
 *  - shaped edge:    {shape: ShapeType, direction: 'in' | 'out'}
 */
export interface EdgeConfig {
    shape: ShapeType | null
    direction: 'flat' | 'in' | 'out'
}

/**
 * Immutable geometry for a puzzle piece. Position and rotation live on the owning Group, not here.
 */
export interface PieceConfig {
    column: number
    row: number
    top: EdgeConfig
    right: EdgeConfig
    bottom: EdgeConfig
    left: EdgeConfig
}

/**
 * A snap-group of one or more pieces. Initially every piece is its own single-piece group.
 * All position/rotation state lives here. The pieces array is a list of grid positions.
 */
export interface Group {
    id: string
    pieces: Array<{ column: number, row: number }>
    rotation: number // 0, 90, 180, 270
    x: number
    y: number
}

export const FLAT: EdgeConfig = {shape: null, direction: 'flat'}

// Canonical point: [along, perp] relative to an edge
type CanonicalPoint = [number, number]
type ShapeGenerator = (size: number, tabHeight: number) => CanonicalPoint[]

export const IMAGE_WIDTH = 1376
export const IMAGE_HEIGHT = 768
export const PUZZLE_SCALE = 0.8

const TAB_HEIGHT_RATIO = 0.08
const EDGE_MARGIN_RATIO = 0.15

export function calculatePieceDimensions(cols: number, rows: number): { pieceWidth: number, pieceHeight: number } {
    return {
        pieceWidth: IMAGE_WIDTH * PUZZLE_SCALE / cols,
        pieceHeight: IMAGE_HEIGHT * PUZZLE_SCALE / rows,
    }
}

/** Return the "along" length for a given edge type. Top/bottom run along width; left/right along height. */
function edgeLength(edge: EdgeType, pieceWidth: number, pieceHeight: number): number {
    return (edge === 'top' || edge === 'bottom') ? pieceWidth : pieceHeight
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

// --- Edge mapper ---
// Map canonical (along, perp) to canvas (x, y).
// Positive perp points outward from the piece center.

type EdgeMapper = (along: number, perp: number) => [number, number]

export function getEdgeMapper(edge: EdgeType, originX: number, originY: number, width: number, height: number): EdgeMapper {
    switch (edge) {
        case 'top':
            return (along, perp) => [originX + along, originY - perp]
        case 'right':
            return (along, perp) => [originX + width + perp, originY + along]
        case 'bottom':
            return (along, perp) => [originX + width - along, originY + height + perp]
        case 'left':
            return (along, perp) => [originX - perp, originY + height - along]
    }
}

// --- Path builder ---

/** Round to 2 decimal places to eliminate floating-point drift between matching in/out edges. */
function round(n: number): number {
    return Math.round(n * 100) / 100
}

export function buildPiecePath(originX: number, originY: number, pieceWidth: number, pieceHeight: number, config: PieceConfig): string {
    let pathString = `M ${round(originX)} ${round(originY)}`

    const edges: Array<{ name: EdgeType, edgeConfig: EdgeConfig }> = [
        {name: 'top', edgeConfig: config.top},
        {name: 'right', edgeConfig: config.right},
        {name: 'bottom', edgeConfig: config.bottom},
        {name: 'left', edgeConfig: config.left},
    ]

    for (const {name, edgeConfig} of edges) {
        const mapper = getEdgeMapper(name, originX, originY, pieceWidth, pieceHeight)
        const length = edgeLength(name, pieceWidth, pieceHeight)
        const tabHeight = length * TAB_HEIGHT_RATIO

        if (edgeConfig.direction === 'flat') {
            const [endX, endY] = mapper(length, 0)
            pathString += ` L ${round(endX)} ${round(endY)}`
        } else {
            const direction = edgeConfig.direction === 'out' ? 1 : -1
            const points = SHAPES[edgeConfig.shape!](length, tabHeight)
            for (const [along, perp] of points) {
                const [x, y] = mapper(along, perp * direction)
                pathString += ` L ${round(x)} ${round(y)}`
            }
            const [endX, endY] = mapper(length, 0)
            pathString += ` L ${round(endX)} ${round(endY)}`
        }
    }

    pathString += ' Z'
    return pathString
}

/** Build the outline of a piece as a flat array of [x0,y0,x1,y1,...] numbers, suitable for PIXI.Polygon. */
export function buildPiecePoints(originX: number, originY: number, pieceWidth: number, pieceHeight: number, config: PieceConfig): number[] {
    const out: number[] = []
    out.push(round(originX), round(originY))

    const edges: Array<{ name: EdgeType, edgeConfig: EdgeConfig }> = [
        {name: 'top', edgeConfig: config.top},
        {name: 'right', edgeConfig: config.right},
        {name: 'bottom', edgeConfig: config.bottom},
        {name: 'left', edgeConfig: config.left},
    ]

    for (const {name, edgeConfig} of edges) {
        const mapper = getEdgeMapper(name, originX, originY, pieceWidth, pieceHeight)
        const length = edgeLength(name, pieceWidth, pieceHeight)
        const tabHeight = length * TAB_HEIGHT_RATIO

        if (edgeConfig.direction === 'flat') {
            const [endX, endY] = mapper(length, 0)
            out.push(round(endX), round(endY))
        } else {
            const direction = edgeConfig.direction === 'out' ? 1 : -1
            const points = SHAPES[edgeConfig.shape!](length, tabHeight)
            for (const [along, perp] of points) {
                const [x, y] = mapper(along, perp * direction)
                out.push(round(x), round(y))
            }
            const [endX, endY] = mapper(length, 0)
            out.push(round(endX), round(endY))
        }
    }

    return out
}

/**
 * Build an SVG path string for just the flat (border) edges of a piece.
 * Returns null if the piece has no flat edges (center piece).
 * Includes invisible anchor points at the main path's bounding box corners
 * so that fabric.js computes the same pathOffset/dimensions as the main path.
 */
export function buildBorderPath(originX: number, originY: number, pieceWidth: number, pieceHeight: number, config: PieceConfig): string | null {
    const edges: Array<{ name: EdgeType, edgeConfig: EdgeConfig }> = [
        {name: 'top', edgeConfig: config.top},
        {name: 'right', edgeConfig: config.right},
        {name: 'bottom', edgeConfig: config.bottom},
        {name: 'left', edgeConfig: config.left},
    ]

    const hasBorder = edges.some(e => e.edgeConfig.direction === 'flat')
    if (!hasBorder) return null

    // Compute the full bounding box to match the main path.
    // The main path always starts at (0,0) min because extensions push the origin inward.
    const vTabHeight = pieceHeight * TAB_HEIGHT_RATIO // tab height for vertical edges (left/right)
    const hTabHeight = pieceWidth * TAB_HEIGHT_RATIO  // tab height for horizontal edges (top/bottom)
    const extRight = config.right.direction === 'out' ? vTabHeight : 0
    const extBottom = config.bottom.direction === 'out' ? hTabHeight : 0
    const maxX = round(originX + pieceWidth + extRight)
    const maxY = round(originY + pieceHeight + extBottom)

    // Anchor M commands at opposite corners to force same bounding box as main path
    let pathString = `M 0 0 M ${maxX} ${maxY}`

    for (const {name, edgeConfig} of edges) {
        if (edgeConfig.direction !== 'flat') continue
        const mapper = getEdgeMapper(name, originX, originY, pieceWidth, pieceHeight)
        const length = edgeLength(name, pieceWidth, pieceHeight)
        const [startX, startY] = mapper(0, 0)
        const [endX, endY] = mapper(length, 0)
        pathString += ` M ${round(startX)} ${round(startY)} L ${round(endX)} ${round(endY)}`
    }

    return pathString
}

// Piece/edge generation has moved to the backend (JigsawCreator.kt); the client receives a
// pre-generated PieceConfig[] + initial Group[] in the SERVER_JIGSAW_ENTER payload.
