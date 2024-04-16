import {SweeperCellModifier, SweeperCellType, SweeperImage} from "./SweeperModel";
import {Point} from "./SweeperServerActionProcessor";


export const serverGridToGameStateGrid = (serverGrid: string[]): SweeperCellType[][] => {
    const mineLocations: Point[] = getMineLocations(serverGrid)
    const grid: SweeperCellType[][] = []

    for (let y = 0; y < serverGrid.length; y++) {
        const row: SweeperCellType[] = []
        for (let x = 0; x < serverGrid[0].length; x++) {
            const value = serverGrid[y][x]
            const status = determineStatus(x, y, value, mineLocations)
            row.push(status)
        }
        grid.push(row)
    }
    return grid
}

function getMineLocations(serverGrid: string[]) {
    const locations: Point[] = []
    for (let y = 0; y < serverGrid.length; y++) {
        for (let x = 0; x < serverGrid[0].length; x++) {
            const value = serverGrid[y][x]
            if (value === "*" || value === "c" || value === "!" ) {
                locations.push({x, y})
            }
        }
    }
    return locations;
}

function determineStatus(x: number, y: number, value: string, mineLocations: Point[]): SweeperCellType {
    if (value === "c" || value == "*") {
        return SweeperCellType.MINE
    }

    const count = countMinesAround(x, y, mineLocations)
    if (count === 0) {
        return SweeperCellType.EMPTY
    }
    return `W${count}` as SweeperCellType
}

function countMinesAround(x: number, y: number, mineLocations: Point[]): number {
    return mineLocations.filter((point: Point) => {
        return isAdjacent(x, y, point)
    }).length
}

function isAdjacent(x: number, y: number, point: Point) {
    const dx = Math.abs(x - point.x);
    const dy = Math.abs(y - point.y);
    return dx <= 1 && dy <= 1
}



export const serverGridToModifiers = (serverGrid: string[]) => {
    const parsedGrid: SweeperCellModifier[][] = []
    for (let y = 0; y < serverGrid.length; y++) {
        const row: SweeperCellModifier[] = []
        for (let x = 0; x < serverGrid[0].length; x++) {
            const value = serverGrid[y][x]

            const result = parseModifierFromGrid(value)
            row.push(result)
        }
        parsedGrid.push(row)
    }
    return parsedGrid
}

const parseModifierFromGrid = (value: string): SweeperCellModifier => {
    if (value === "c" || value === "i") {
        return SweeperCellModifier.FLAG
    }
    if (value === "!" || value === "?") {
        return SweeperCellModifier.QUESTION_MARK
    }
    if (value === "." || value === "*") {
        return SweeperCellModifier.UNKNOWN
    }
    if (value === "-" ) {
        return SweeperCellModifier.REVEALED
    }

    throw Error("Unknown sweeper cell modifier for server grid value: " + value)
}

export const determineRevealedCells = (grid: SweeperImage[][], revealed: boolean[][], flagged: boolean[][], x: number, y: number): Point[] => {
    // don't reveal flagged cells or already revealed cells
    if (revealed[y][x] || flagged[y][x]) {
        return []
    }
    // reveal mined
    if (grid[y][x] !== SweeperImage.EMPTY) {
        return [{x, y}]
    }

    const explored = new Set<string>()
    const found = exploreAround(grid, x, y, explored, revealed, flagged)
    const foundPoints: Point[] = []
    found.forEach((s: string) => {
        const parts = s.split(":")
        foundPoints.push({x: parseInt(parts[0]), y: parseInt(parts[1])})
    })
    return foundPoints
}

function exploreAround(grid: SweeperImage[][], x: number, y: number, explored: Set<string>, revealed: boolean[][], flagged: boolean[][]): Set<string> {
    const foundN = exploreDirection(grid, x, y, explored, 0, -1, revealed, flagged)
    const foundS = exploreDirection(grid, x, y, explored, 0, 1, revealed, flagged)
    const foundE = exploreDirection(grid, x, y, explored, 1, 0, revealed, flagged)
    const foundW = exploreDirection(grid, x, y, explored, -1, 0, revealed, flagged)
    return new Set([`${x}:${y}`, ...foundN, ...foundS, ...foundE, ...foundW])
}

function exploreDirection(grid: SweeperImage[][], x: number, y: number, explored:Set<string>, dx: number, dy: number, revealed: boolean[][], flagged: boolean[][]): Set<string> {

    const newX = x + dx
    const newY = y + dy

    if (newX < 0 || newY < 0 || newX >= grid[0].length || newY >= grid.length) {
        return new Set<string>()
    }
    if (revealed[newY][newX] || flagged[newY][newX]) {
        return new Set<string>()
    }
    const here = `${newX}:${newY}`
    if (explored.has(here)) {
        return new Set<string>()
    }
    explored.add(here)

    if (grid[newY][newX] === SweeperImage.EMPTY) {
        return exploreAround(grid, newX, newY, explored, revealed, flagged)
    }
    return new Set<string>([here])
}
