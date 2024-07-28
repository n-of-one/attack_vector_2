import {AnyAction} from "redux"
import {BACKSPACE, DOWN, TAB, UP} from "../util/KeyCodes"
import {SERVER_ERROR} from "../../hacker/server/GenericServerActionProcessor"
import {parseTextToTerminalLine} from "./TerminalLineParser";

const LINE_LIMIT = 100
const TERMINAL_UPDATES_PER_TICK = 4

export const SERVER_TERMINAL_SYNTAX_HIGHLIGHTING = "SERVER_TERMINAL_SYNTAX_HIGHLIGHTING"
export const TERMINAL_KEY_PRESS = "TERMINAL_KEY_PRESS"
export const TERMINAL_SUBMIT = "TERMINAL_SUBMIT"
export const TICK = "TICK"
export const TERMINAL_RECEIVE = "TERMINAL_RECEIVE"
export const SERVER_TERMINAL_UPDATE_PROMPT = "SERVER_TERMINAL_UPDATE_PROMPT"
export const SERVER_TERMINAL_RECEIVE = "SERVER_TERMINAL_RECEIVE"
export const TERMINAL_CLEAR = "TERMINAL_CLEAR"
export const TERMINAL_LOCK = "TERMINAL_LOCK"
export const TERMINAL_UNLOCK = "TERMINAL_UNLOCK"
export const TERMINAL_REPLACE_LAST_LINE = "TERMINAL_REMOVE_LAST_LINE"


export interface TerminalLineData {
    blocks: TerminalLineBlock[]
    key: number
}


export enum TerminalBlockType {
    TEXT,
    SPACE,
    LINK,
    EMPTY_LINE,
    IMAGE
}

export interface TerminalLineBlock {
    type: TerminalBlockType
    text: string
    size: number
    key: number,
    className: string
    link?: string
    imageSource?: string
}


export interface Syntax {
    main: string[],
    rest: string
}

export type SyntaxMap = { [key: string]: Syntax }

export interface TerminalState {
    id: string,

    prompt: string,
    blockedWhileRendering: boolean,
    renderOutput: boolean,  /// if false it means input only
    readOnly: boolean,
    input: string

    autoScroll: boolean,
    renderedLines: TerminalLineData[],

    renderingLine: TerminalLineData | null
    renderingLineBlockIndex: number
    renderingBlockInsideIndex: number
    unrenderedLines: TerminalLineData[],

    syntaxHighlighting: SyntaxMap,
    history: string[]
    historyIndex: number
}

export const terminalStateDefault: TerminalState = {
    id: "",

    prompt: "⇀ ",
    blockedWhileRendering: false, // Use for terminal to block input while "command" is "running".
    renderOutput: true,     // If false, this Terminal acts like an input prompt
    readOnly: false,        // only allow user input if true
    input: "",              // user input

    autoScroll: false,      // scroll to bottom one new info.
    renderedLines: [],              // lines that are fully shown

    renderingLine: null,    // String - part of the current rendering line that is being shown
    renderingLineBlockIndex: 0,
    renderingBlockInsideIndex: 0,
    unrenderedLines: [],
    syntaxHighlighting: {},
    history: [],            // [ "move 00", "view", "hack 2" ]
    historyIndex: 0         // which history item was last selected. When historyIndex == history then no history item is selected
}

interface CreatTerminalConfig {
    readOnly?: boolean,
    autoScroll?: boolean,
    blockedWhileRendering?: boolean
    renderOutput?: boolean,
}

type TerminalReducer = (state: TerminalState | undefined, action: AnyAction) => TerminalState

export const createTerminalReducer = (id: string, config: CreatTerminalConfig): TerminalReducer => {
    const defaultState = {...terminalStateDefault, ...config, id: id}

    return (state: TerminalState | undefined = defaultState, action: AnyAction): TerminalState => {
        if (action.type === TICK) {
            let currentState = state
            for (let i = 0; i < TERMINAL_UPDATES_PER_TICK; i++) {
                currentState = processUpdate(currentState)
            }
            return currentState
        }

        // terminalId from server actions is in data part
        const terminalIdFromAction = (action.terminalId) ? action.terminalId : action.data?.terminalId

        if (terminalIdFromAction !== state.id) {
            return state
        }

        switch (action.type) {
            case TERMINAL_RECEIVE:
                return receive(state, action as unknown as TerminalLineReceiveAction)
            case SERVER_TERMINAL_RECEIVE:
                return receiveFromServer(state, action)
            case TERMINAL_KEY_PRESS:
                return handlePressKey(state, action)
            case TERMINAL_SUBMIT:
                return handlePressEnter(state, action as unknown as TerminalSubmitActionData)
            case SERVER_ERROR:
                return handleServerError(state, action)
            case TERMINAL_CLEAR:
                return handleTerminalClear(state, action)
            case TERMINAL_LOCK:
                return terminalSetReadonly(state, true)
            case TERMINAL_UNLOCK:
                return terminalSetReadonly(state, false)
            case SERVER_TERMINAL_SYNTAX_HIGHLIGHTING:
                return processSyntaxHighlighting(state, action.data)
            case TERMINAL_REPLACE_LAST_LINE:
                return replaceLastLine(state, action.data)
            case SERVER_TERMINAL_UPDATE_PROMPT:
                return updatePrompt(state, action.data)
            default:
                return state
        }
    }
}

function processUpdate(state: TerminalState): TerminalState {
    if (state.renderingLine === null && state.unrenderedLines.length === 0) {
        // all lines are rendered, nothing to do
        return state
    }

    const nextState: TerminalState = {
        ...state
    }

    // start on the next line if we were not rendering a line already
    if (nextState.renderingLine === null) {
        nextState.renderingLine = state.unrenderedLines[0]
        nextState.unrenderedLines = [...state.unrenderedLines].splice(1)
        nextState.renderingLineBlockIndex = 0
        nextState.renderingBlockInsideIndex = 0
    }
    const renderingBlock = nextState.renderingLine.blocks[nextState.renderingLineBlockIndex]

    nextState.renderingBlockInsideIndex++

    if (nextState.renderingBlockInsideIndex >= renderingBlock.size) {
        // done rendering this block, move to the next
        nextState.renderingBlockInsideIndex = 0
        nextState.renderingLineBlockIndex++

        if (nextState.renderingLineBlockIndex >= nextState.renderingLine.blocks.length) {
            // done rendering this line, move to the next
            nextState.renderedLines = [...state.renderedLines, nextState.renderingLine]

            nextState.renderingLine = null
            // starting on the next line will be handled at the start of this method, on next update.
        }
    }

    return nextState
}

interface TerminalLineReceiveAction {
    data: string
}

const receive = (state: TerminalState, action: TerminalLineReceiveAction): TerminalState => {
    return  {
        ...state,
        unrenderedLines: [...state.unrenderedLines, parseTextToTerminalLine(action.data)]
    }
}


const receiveFromServer = (terminal: TerminalState, action: AnyAction): TerminalState => {
    const serverLines: TerminalLineData[] = (action.data.lines).map((line: string) => parseTextToTerminalLine(line))

    const lockedInput: boolean | undefined = action.data.locked
    const readOnly = (lockedInput !== undefined) ? lockedInput : terminal.readOnly

    return {
        ...terminal,
        readOnly: readOnly,
        unrenderedLines: [...terminal.unrenderedLines, ...serverLines],
    }
}



const handlePressKey = (terminal: TerminalState, action: AnyAction): TerminalState => {
    const {keyCode, key, pastedText} = action
    if (keyCode === UP || keyCode === DOWN) {
        return handleHistory(terminal, keyCode)
    }

    const newInput = determineInput(terminal.input, keyCode, key, pastedText)
    return {...terminal, input: newInput}
}

const determineInput = (input: string, keyCode: number, key: string, pastedText: string | null) => {
    if (pastedText != null) {
        return input + pastedText
    }
    if (keyCode === BACKSPACE && input.length > 0) {
        return input.substr(0, input.length - 1)
    }
    if (keyCode === TAB) {
        return input + "[t]"
    }
    if (key.length === 1) {
        return input + key
    } else {
        return input
    }
}

const handleHistory = (terminal: TerminalState, keyCode: number): TerminalState => {
    const index = terminal.historyIndex + ((keyCode === UP) ? -1 : 1)
    if (index < 0) {
        return terminal
    }
    if (index >= terminal.history.length) {
        return {...terminal, historyIndex: terminal.history.length, input: ""}
    }
    return {...terminal, historyIndex: index, input: terminal.history[index]}

}

export interface TerminalSubmitActionData {
    command: string
}

const handlePressEnter = (terminal: TerminalState, action: TerminalSubmitActionData): TerminalState => {
    const line = terminal.prompt + action.command
    const lines = limitLines([...terminal.renderedLines, parseTextToTerminalLine(`[input]${line}`)])

    const newHistory = limitLines([...terminal.history, (action.command as string)])

    return {
        ...terminal,
        renderedLines: lines,
        input: "",
        history: newHistory,
        historyIndex: newHistory.length,
        readOnly: true
    }
}

/* Only call on mutable array */
const limitLines = (lines: any[]) => {
    while (lines.length > LINE_LIMIT) {
        lines.shift()
    }
    return lines
}

const handleServerError = (terminal: TerminalState, action: AnyAction): TerminalState => {
    const retryLines = (action.data.recoverable) ? [
            parseTextToTerminalLine(""),
            parseTextToTerminalLine("[warn b]A server error occurred. Please refresh.\"}") ] :[
            parseTextToTerminalLine(""),
            parseTextToTerminalLine("[warn b]A fatal server error occurred.")]

    const errorLines = [
        parseTextToTerminalLine(""),
        parseTextToTerminalLine("Details: " + action.data.message)]
    return {
        ...terminal,
        input: "",
        unrenderedLines: [...terminal.unrenderedLines, ...retryLines, ...errorLines],
        readOnly: true
    }
}

const handleTerminalClear = (terminal: TerminalState, action: AnyAction) => {
    return {
        ...terminal,
        renderedLines: [],
        unrenderedLines: [],
        input: "",
    }
}

const terminalSetReadonly = (terminal: TerminalState, readOnly: boolean) => {
    return {...terminal, readOnly: readOnly}
}

const processSyntaxHighlighting = (terminal: TerminalState, data: any) => {
    return {...terminal, syntaxHighlighting: data.highlighting}
}

const replaceLastLine = (terminal: TerminalState, line: string) => {
    const lines = [...terminal.renderedLines]
    if (terminal.renderedLines.length > 0) {
        lines.pop()
    }
    lines.push(parseTextToTerminalLine(line))
    return {...terminal, lines: lines}
}

interface TerminalUpdatePrompt {
    prompt: string
}

const updatePrompt = (terminal: TerminalState, data: TerminalUpdatePrompt) => {
    return {...terminal, prompt: data.prompt}
}
