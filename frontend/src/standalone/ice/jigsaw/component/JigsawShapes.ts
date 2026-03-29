// --- Types ---

export type ShapeType = 'trapezoid' | 'parapet' | 'saw' | 'triangle'
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
    top: EdgeConfig
    right: EdgeConfig
    bottom: EdgeConfig
    left: EdgeConfig
}

export const FLAT: FlatEdge = {dir: 'flat'}

// Canonical point: [along, perp] relative to an edge
type CanonicalPoint = [number, number]
type ShapeGenerator = (size: number, tabHeight: number, chamferSize: number) => CanonicalPoint[]

const TAB_HEIGHT_RATIO = 0.2
const CHAMFER_RATIO = 0.15

// --- Shape definitions ---
// Each shape returns [along, perp] points in canonical form:
//   along = pixels along the edge from start corner
//   perp  = pixels outward from the piece (positive = away from center)

export const SHAPES: Record<ShapeType, ShapeGenerator> = {
    trapezoid: (size, tabHeight, chamferSize) => [
        [size * 0.4, 0],
        [size * 0.31, tabHeight - chamferSize],
        [size * 0.3 + chamferSize, tabHeight],
        [size * 0.7 - chamferSize, tabHeight],
        [size * 0.69, tabHeight - chamferSize],
        [size * 0.6, 0],
    ],
    parapet: (size, tabHeight, chamferSize) => [
        [size * 0.385, 0],
        [size * 0.35, tabHeight * 0.3],
        [size * 0.35, tabHeight - chamferSize],
        [size * 0.35 + chamferSize, tabHeight],
        [size * 0.45, tabHeight],
        [size * 0.45, tabHeight * 0.5],
        [size * 0.55, tabHeight * 0.5],
        [size * 0.55, tabHeight],
        [size * 0.65 - chamferSize, tabHeight],
        [size * 0.65, tabHeight - chamferSize],
        [size * 0.65, tabHeight * 0.3],
        [size * 0.615, 0],
    ],
    saw: (size, tabHeight, _chamferSize) => [
        [size * 0.365, 0],
        [size * 0.35, tabHeight * 0.5],
        [size * 0.38, tabHeight],
        [size * 0.42, tabHeight],
        [size * 0.45, tabHeight * 0.5],
        [size * 0.50, tabHeight],
        [size * 0.55, tabHeight * 0.5],
        [size * 0.58, tabHeight],
        [size * 0.62, tabHeight],
        [size * 0.65, tabHeight * 0.5],
        [size * 0.635, 0],
    ],
    triangle: (size, tabHeight, _chamferSize) => [
        [size * 0.35, 0],
        [size * 0.32, tabHeight * 0.3],
        [size * 0.45, tabHeight],
        [size * 0.55, tabHeight],
        [size * 0.68, tabHeight * 0.3],
        [size * 0.65, 0],
    ],
}

const ALL_SHAPES: ShapeType[] = ['trapezoid', 'parapet', 'saw', 'triangle']

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

export function buildPiecePath(originX: number, originY: number, size: number, config: PieceConfig): string {
    const tabHeight = size * TAB_HEIGHT_RATIO
    const chamferSize = tabHeight * CHAMFER_RATIO
    let pathString = `M ${originX} ${originY}`

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
            pathString += ` L ${endX} ${endY}`
        } else {
            const direction = edgeConfig.dir === 'out' ? 1 : -1
            const points = SHAPES[edgeConfig.shape](size, tabHeight, chamferSize)
            for (const [along, perp] of points) {
                const [x, y] = mapper(along, perp * direction)
                pathString += ` L ${x} ${y}`
            }
            const [endX, endY] = mapper(size, 0)
            pathString += ` L ${endX} ${endY}`
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
