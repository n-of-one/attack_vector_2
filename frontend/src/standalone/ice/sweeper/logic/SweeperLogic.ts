import {IceStrength} from "../../../../common/model/IceStrength";

/**
 Cells is a string representation of the board, regardless of visibility or modifiers like flags.
 mine: *
 empty: 0
 adjacent to mine: 1-8

 For example:
 *1001**
 1211122
 01*1000
 0111000

 Modifiers is a string representation of the modifiers:
 h hidden
 r revealed
 f flag

 For example:
 rrrrrrf
 rrrrrrr
 rrhrrrr
 rrrrrrr

 Note that in this example, the top left mine has exploded, as it has been revealed
 */


export enum SweeperCellType {
    MINE = "MINE",
    EMPTY = "0",
    M1 = "1",
    M2 = "2",
    M3 = "3",
    M4 = "4",
    M5 = "5",
    M6 = "6",
    M7 = "7",
    M8 = "8",
}

export enum SweeperCellModifier {
    HIDDEN = "HIDDEN",
    REVEALED = "REVEALED",
    FLAG = "FLAG",
}

export interface SweeperGameState {
    cells: SweeperCellType[][]
    modifiers: SweeperCellModifier[][]
    strength: IceStrength
    blockedUserIds: string[]
    minesLeft: number
}


export const serverCellsToGameCells = (serverCells: string[]): SweeperCellType[][] => {
    return serverCells.map((row) => {
            return row.split("").map((cellValue) => {
                return serverCellToType(cellValue)
            })
        }
    )
}

const serverCellToType = (value: string): SweeperCellType => {
    // * is mine, 0-8 is the number of adjacent mines (0 is empty)
    if (value === "*") {return SweeperCellType.MINE}
    return value as SweeperCellType
}


export const serverModifiersToGameModifiers = (serverModifiers: string[]) => {
    // . is unknown, - is revealed, f is flag, ? is question mark
    return serverModifiers.map((row) => {
    return row.split("").map((modifierValue) => {
                return serverCellToModifier(modifierValue)
            })
    })
}

const serverCellToModifier = (value: string): SweeperCellModifier => {
    switch(value) {
        case "h": return SweeperCellModifier.HIDDEN
        case "r": return SweeperCellModifier.REVEALED
        case "f": return SweeperCellModifier.FLAG
        default: throw Error("Unknown sweeper cell modifier for server grid value: " + value)
    }
}

