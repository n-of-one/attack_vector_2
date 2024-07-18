import {SweeperCellModifier, SweeperCellType, SweeperImageType} from "./SweeperModel";
import {Point} from "./SweeperServerActionProcessor";


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
        case ".": return SweeperCellModifier.UNREVEALED
        case "-": return SweeperCellModifier.REVEALED
        case "f": return SweeperCellModifier.FLAG
        case "?": return SweeperCellModifier.QUESTION_MARK
        default: throw Error("Unknown sweeper cell modifier for server grid value: " + value)
    }
}

