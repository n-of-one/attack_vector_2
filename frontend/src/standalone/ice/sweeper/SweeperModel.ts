import {IceStrength} from "../../../common/model/IceStrength";

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
    UNREVEALED = "UNREVEALED",
    REVEALED = "REVEALED",
    FLAG = "FLAG",
    QUESTION_MARK = "QUESTION_MARK",
}

export interface SweeperGameState {
    cells: SweeperCellType[][]
    modifiers: SweeperCellModifier[][]
    strength: IceStrength
    hacked: boolean
    userBlocked: boolean
}

export enum SweeperImageType {
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
    FLAG = "FLAG",
    UNKNOWN = "UNKNOWN",
    QUESTION_MARK = "QUESTION_MARK",
}
