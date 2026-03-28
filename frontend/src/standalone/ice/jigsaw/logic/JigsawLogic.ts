// --- Types ---

export type ShapeType = 'trapezoid' | 'parapet' | 'saw' | 'triangle'

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
    top: EdgeConfig
    right: EdgeConfig
    bottom: EdgeConfig
    left: EdgeConfig
}

export const FLAT: FlatEdge = {dir: 'flat'}

// Canonical point: [along, perp] relative to an edge
type CanonicalPoint = [number, number]
type ShapeGenerator = (size: number, tab: number, c: number) => CanonicalPoint[]

// --- Shape definitions ---
// Each shape returns [along, perp] points in canonical form:
//   along = pixels along the edge from start corner
//   perp  = pixels outward from the piece (positive = away from center)

export const SHAPES: Record<ShapeType, ShapeGenerator> = {
    trapezoid: (size, tab, c) => [
        [size * 0.4, 0],
        [size * 0.31, tab - c],
        [size * 0.3 + c, tab],
        [size * 0.7 - c, tab],
        [size * 0.69, tab - c],
        [size * 0.6, 0],
    ],
    parapet: (size, tab, c) => [
        [size * 0.385, 0],
        [size * 0.35, tab * 0.3],
        [size * 0.35, tab - c],
        [size * 0.35 + c, tab],
        [size * 0.45, tab],
        [size * 0.45, tab * 0.5],
        [size * 0.55, tab * 0.5],
        [size * 0.55, tab],
        [size * 0.65 - c, tab],
        [size * 0.65, tab - c],
        [size * 0.65, tab * 0.3],
        [size * 0.615, 0],
    ],
    saw: (size, tab, _c) => [
        [size * 0.365, 0],
        [size * 0.35, tab * 0.5],
        [size * 0.38, tab],
        [size * 0.42, tab],
        [size * 0.45, tab * 0.5],
        [size * 0.50, tab],
        [size * 0.55, tab * 0.5],
        [size * 0.58, tab],
        [size * 0.62, tab],
        [size * 0.65, tab * 0.5],
        [size * 0.635, 0],
    ],
    triangle: (size, tab, _c) => [
        [size * 0.35, 0],
        [size * 0.32, tab * 0.3],
        [size * 0.45, tab],
        [size * 0.55, tab],
        [size * 0.68, tab * 0.3],
        [size * 0.65, 0],
    ],
}

const ALL_SHAPES: ShapeType[] = ['trapezoid', 'parapet', 'saw', 'triangle']

// --- Edge mapper ---
// Map canonical (along, perp) to canvas (x, y).
// Positive perp points outward from the piece center.

type EdgeMapper = (along: number, perp: number) => [number, number]

export function getEdgeMapper(edge: string, ox: number, oy: number, size: number): EdgeMapper {
    switch (edge) {
        case 'top':
            return (a, p) => [ox + a, oy - p]
        case 'right':
            return (a, p) => [ox + size + p, oy + a]
        case 'bottom':
            return (a, p) => [ox + size - a, oy + size + p]
        case 'left':
            return (a, p) => [ox - p, oy + size - a]
        default:
            throw new Error(`Invalid edge: ${edge}`)
    }
}

// --- Path builder ---

export function buildPiecePath(ox: number, oy: number, size: number, config: PieceConfig): string {
    const tab = size * 0.2
    const c = tab * 0.15
    let path = `M ${ox} ${oy}`

    const edges: Array<{ name: string, edgeConfig: EdgeConfig }> = [
        {name: 'top', edgeConfig: config.top},
        {name: 'right', edgeConfig: config.right},
        {name: 'bottom', edgeConfig: config.bottom},
        {name: 'left', edgeConfig: config.left},
    ]

    for (const {name, edgeConfig} of edges) {
        const map = getEdgeMapper(name, ox, oy, size)

        if (edgeConfig.dir === 'flat') {
            const [ex, ey] = map(size, 0)
            path += ` L ${ex} ${ey}`
        } else {
            const d = edgeConfig.dir === 'out' ? 1 : -1
            const pts = SHAPES[(edgeConfig as ShapedEdge).shape](size, tab, c)
            for (const [a, p] of pts) {
                const [x, y] = map(a, p * d)
                path += ` L ${x} ${y}`
            }
            const [ex, ey] = map(size, 0)
            path += ` L ${ex} ${ey}`
        }
    }

    path += ' Z'
    return path
}

// --- Grid generation ---

export function flipDir(edge: EdgeConfig): EdgeConfig {
    if (edge.dir === 'flat') return FLAT
    return {shape: (edge as ShapedEdge).shape, dir: edge.dir === 'out' ? 'in' : 'out'}
}

function randomShape(): ShapeType {
    return ALL_SHAPES[Math.floor(Math.random() * ALL_SHAPES.length)]
}

function randomDir(): 'out' | 'in' {
    return Math.random() < 0.5 ? 'out' : 'in'
}

export function generatePieceConfigs(cols: number, rows: number): PieceConfig[] {
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

            pieces.push({col, row, top, right, bottom, left})
        }
    }

    return pieces
}
