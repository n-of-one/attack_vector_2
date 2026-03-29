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

export function generatePieceConfigs(cols: number, rows: number, canvasWidth: number, canvasHeight: number): PieceConfig[] {
    const pieceSize = calculatePieceSize(canvasWidth, canvasHeight, cols, rows)
    const tabSize = pieceSize * TAB_HEIGHT_RATIO
    const margin = tabSize * 2

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

    const pieces: PieceConfig[] = []

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            const top: EdgeConfig = row === 0 ? FLAT : flipDir(verticalEdges[row - 1][col])
            const bottom: EdgeConfig = row === rows - 1 ? FLAT : verticalEdges[row][col]
            const left: EdgeConfig = col === 0 ? FLAT : flipDir(horizontalEdges[row][col - 1])
            const right: EdgeConfig = col === cols - 1 ? FLAT : horizontalEdges[row][col]

            const x = margin + Math.random() * (canvasWidth - pieceSize - margin * 2)
            const y = margin + Math.random() * (canvasHeight - pieceSize - margin * 2)
            const rotation = ROTATIONS[Math.floor(Math.random() * 4)]

            pieces.push({col, row, x, y, rotation, top, right, bottom, left})
        }
    }

    return pieces
}
