import {AnyAction} from "redux"
import {BACKSPACE, DOWN, TAB, UP} from "../../KeyCodes"
import {SERVER_ERROR} from "../../hacker/server/GenericServerActionProcessor"

const LINE_LIMIT = 100

export const SERVER_TERMINAL_SYNTAX_HIGHLIGHTING = "SERVER_TERMINAL_SYNTAX_HIGHLIGHTING"
export const TERMINAL_KEY_PRESS = "TERMINAL_KEY_PRESS"
export const TERMINAL_SUBMIT = "TERMINAL_SUBMIT"
export const TERMINAL_TICK = "TERMINAL_TICK"
export const TERMINAL_RECEIVE = "TERMINAL_RECEIVE"
export const SERVER_TERMINAL_RECEIVE = "SERVER_TERMINAL_RECEIVE"
export const TERMINAL_CLEAR = "TERMINAL_CLEAR"
export const TERMINAL_LOCK = "TERMINAL_LOCK"
export const TERMINAL_UNLOCK = "TERMINAL_UNLOCK"

export interface TerminalLine {
    type: string,
    data: string,
    class?: string[]
}

export interface Syntax {
    main: string[],
    rest: string
}

export type SyntaxMap = { [key: string]: Syntax }

export interface TerminalState {
    id: string,
    lines: TerminalLine[],
    prompt: string,
    readOnly: boolean,
    blockedWhileRendering: boolean,
    renderOutput: boolean,  /// if false it means input only TODO: refactor terminalInput into a separate component.
    autoScroll: boolean,
    input: string,
    renderingLine: TerminalLine | null,
    receivingLine: TerminalLine | null,
    receiveBuffer: TerminalLine[],
    receiving: boolean,
    syntaxHighlighting: SyntaxMap,
    history: string[]
    historyIndex: number
}

export const terminalStateDefault: TerminalState = {
    id: "",
    lines: [],              // lines that are fully shown
    prompt: "⇋ ",
    readOnly: false,        // only allow user input if true
    blockedWhileRendering: false, // Use for terminal to block input while "command" is "running".
    renderOutput: true,     // If false, this Terminal acts like an input prompt
    autoScroll: false,      // scroll to bottom one new info.
    input: "",              // user input
    renderingLine: null,    // String - part of the current rendering line that is being shown
    receivingLine: null,    // String - part of the current rendering line that is waiting to be shown
    receiveBuffer: [{type: "text", data: "[b]🜁 Verdant OS 🜃"}, {type: "text", data: " "}], // lines waiting to be shown.
    receiving: true,        // true if there are lines waiting to be shown, or in the process of being shown.
    syntaxHighlighting: {},
    history: [],            // [ "move 00", "view", "hack 2" ]
    historyIndex: 0         // which history item was last selected. When historyIndex == history then no history item is selected
}

interface CreatTerminalConfig {
    readOnly?: boolean,
    receiveBuffer?: TerminalLine[],
    autoScroll?: boolean,
    blockedWhileRendering?: boolean
    renderOutput?: boolean,
}

type TerminalReducer = (state: TerminalState | undefined, action: AnyAction) => TerminalState

export const createTerminalReducer = (id: string, config: CreatTerminalConfig): TerminalReducer => {
    const defaultState = {...terminalStateDefault, ...config, id: id}

    return (terminal: TerminalState | undefined = defaultState , action: AnyAction) => {
        if (action.type === TERMINAL_TICK) {
            return processTick(processTick(processTick(processTick(terminal))))
        }

        // terminalId from server actions is in data part
        const terminalIdFromAction = (action.terminalId) ? action.terminalId : action.data?.terminalId

        if (terminalIdFromAction !== terminal.id) {
            return terminal
        }

        switch (action.type) {
            case TERMINAL_RECEIVE:
                return receive(terminal, action)
            case SERVER_TERMINAL_RECEIVE:
                return receiveFromServer(terminal, action)
            case TERMINAL_KEY_PRESS:
                return handlePressKey(terminal, action)
            case TERMINAL_SUBMIT:
                return handlePressEnter(terminal, action as unknown as TerminalSubmitActionData)
            case SERVER_ERROR:
                return handleServerError(terminal, action)
            case TERMINAL_CLEAR:
                return handleTerminalClear(terminal, action, defaultState)
            case TERMINAL_LOCK:
                return terminalSetReadonly(terminal, true)
            case TERMINAL_UNLOCK:
                return terminalSetReadonly(terminal, false)
            case SERVER_TERMINAL_SYNTAX_HIGHLIGHTING:
                return processSyntaxHighlighting(terminal, action.data)
            default:
                return terminal
        }
    }
}

function processTick(terminal: TerminalState) {
    if (!terminal.receiving) {
        return terminal
    }
    if (terminal.receivingLine === null && terminal.receiveBuffer.length === 0) {
        return {...terminal, receiving: false}
    }

    let nextReceiveBuffer = [...terminal.receiveBuffer]
    let nextLines = [...terminal.lines]
    let nextRenderingLine: TerminalLine | null = (terminal.renderingLine) ? {...terminal.renderingLine} : {type: "text", data: ""}

    let nextReceivingLine
    if (terminal.receivingLine !== null) {
        nextReceivingLine = {...terminal.receivingLine}
    } else {
        nextReceivingLine = {...nextReceiveBuffer[0]}
        nextReceiveBuffer = [...terminal.receiveBuffer].splice(1)
    }

    let input = nextReceivingLine.data

    if (input === "") {
        nextReceivingLine.data = ""
        nextRenderingLine.data = ""
    } else {
        const nextChar = input.substr(0, 1)
        const nextData = input.substr(1)

        nextReceivingLine.data = nextData
        nextRenderingLine.data += nextChar
    }

    let nextReceiving = true
    if (nextReceivingLine.data.length === 0) {
        // finish rendering this line
        nextLines = limitLines([...terminal.lines, nextRenderingLine])
        nextRenderingLine = null
        nextReceivingLine = null
        if (nextReceiveBuffer.length === 0) {
            nextReceiving = false
        }
    }

    const nextState = {
        ...terminal,
        renderingLine: nextRenderingLine,
        receivingLine: nextReceivingLine,
        lines: nextLines,
        receiving: nextReceiving,
        receiveBuffer: nextReceiveBuffer,
    }
    return nextState
}


const receive = (terminal: TerminalState, action: AnyAction) => {
    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : []

    const line = {type: "text", data: action.data}

    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, line],
    }
}

const receiveFromServer = (terminal: TerminalState, action: AnyAction) => {
    const lines = action.data.lines.map((line: TerminalLine) => {
        return {type: "text", data: line}
    })

    const lockedInput: boolean | undefined = action.data.locked
    const readOnly = (lockedInput !== undefined) ? lockedInput : terminal.readOnly

    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : []
    return {
        ...terminal,
        receiving: true,
        readOnly: readOnly,
        receiveBuffer: [...buffer, ...lines],
    }
}

const handlePressKey = (terminal: TerminalState, action: AnyAction): TerminalState => {
    const {keyCode, key} = action
    if (keyCode === UP || keyCode === DOWN) {
        return handleHistory(terminal, keyCode)
    }

    const newInput = determineInput(terminal.input, keyCode, key)
    return {...terminal, input: newInput}
}

const determineInput = (input: string, keyCode: number, key: string) => {
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
    if  (index >= terminal.history.length) {
        return {...terminal, historyIndex: terminal.history.length, input: "" }
    }
    return {...terminal, historyIndex: index, input: terminal.history[index] }

}

export interface TerminalSubmitActionData {
    command: string
}
const handlePressEnter = (terminal: TerminalState, action: TerminalSubmitActionData): TerminalState => {
    const line = terminal.prompt + action.command
    const lines = limitLines([...terminal.lines, {type: "text", data: line, class: ["input"]}])

    const newHistory = limitLines([...terminal.history, (action.command as string)])

    return {...terminal, lines: lines, input: "", receiving: true, history: newHistory, historyIndex: newHistory.length}
}

/* Only call on mutable array */
const limitLines = (lines: any[]) => {
    while (lines.length > LINE_LIMIT) {
        lines.shift()
    }
    return lines
}

const handleServerError = (terminal: TerminalState, action: AnyAction) => {
    const retryLines = (action.data.recoverable) ? [
            {type: "text", data: " "},
            {type: "text", data: "[warn b]A server error occurred. Please refresh browser."}]
        : [
            {type: "text", data: " "},
            {type: "text", data: "[warn b]A fatal server error occurred."}]


    const errorLines = [
        {type: "text", data: " "},
        {type: "text", data: "Details: " + action.data.message}
    ]
    return {
        ...terminal,
        input: "",
        receiveInput: null,
        receiveBuffer: [...terminal.receiveBuffer, ...retryLines, ...errorLines],
        receiving: true,
        readOnly: true
    }
}

const handleTerminalClear = (terminal: TerminalState, action: AnyAction, defaultState: TerminalState) => {
    return defaultState
}

const terminalSetReadonly = (terminal: TerminalState, readOnly: boolean) => {
    return {...terminal, readOnly: readOnly}
}

const processSyntaxHighlighting = (terminal: TerminalState, data: any) => {
    return {...terminal, syntaxHighlighting: data.highlighting}
}
