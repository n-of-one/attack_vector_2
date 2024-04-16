import {IceStrength} from "../../../common/model/IceStrength";

export enum SweeperCellType {
    EMPTY = "EMPTY",
    MINE = "MINE",
    W1 = "W1",
    W2 = "W2",
    W3 = "W3",
    W4 = "W4",
    W5 = "W5",
    W6 = "W6",
    W7 = "W7",
    W8 = "W8",
}

export enum SweeperCellModifier {
    UNKNOWN = "UNKNOWN",
    REVEALED = "REVEALED",
    FLAG = "FLAG",
    QUESTION_MARK = "QUESTION_MARK",
}

export interface SweeperGameState {
    cells: SweeperCellType[][]
    modifiers: SweeperCellModifier[][]
    strength: IceStrength
    hacked: boolean
}

export enum SweeperImage {
    EMPTY = "EMPTY",
    MINE = "MINE",
    W1 = "W1",
    W2 = "W2",
    W3 = "W3",
    W4 = "W4",
    W5 = "W5",
    W6 = "W6",
    W7 = "W7",
    W8 = "W8",
    FLAG = "FLAG",
    UNKNOWN = "UNKNOWN",
    QUESTION_MARK = "QUESTION_MARK",
}
