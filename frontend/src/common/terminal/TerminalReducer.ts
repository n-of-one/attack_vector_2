import {
    SERVER_TERMINAL_RECEIVE,
    SERVER_TERMINAL_SYNTAX_HIGHLIGHTING,
    TERMINAL_CLEAR,
    TERMINAL_KEY_PRESS,
    TERMINAL_LOCK,
    TERMINAL_RECEIVE,
    TERMINAL_SUBMIT,
    TERMINAL_TICK,
    TERMINAL_UNLOCK
} from "./TerminalActions";
import {SERVER_ERROR} from "../enums/CommonActions";
import {BACKSPACE, DOWN, TAB, UP} from "../../KeyCodes";
import {AnyAction} from "redux";

const LINE_LIMIT = 100;

export interface TerminalLine {
    type: string,
    data: string,
    class?: string[]
}

export interface Syntax {
    first: string,
    second: string,
    rest: string
}

export interface TerminalState {
    id: string,
    lines: TerminalLine[],
    prompt: string,
    readOnly: boolean,
    renderOutput: boolean,
    autoScroll: boolean,
    input: string,
    renderingLine: TerminalLine | null,
    receivingLine: TerminalLine | null,
    receiveBuffer: TerminalLine[],
    receiving: boolean,
    syntaxHighlighting: { [key: string]: Syntax },
    history: string[]
    historyIndex: number
}

export const terminalStateDefault: TerminalState = {
    id: "",
    lines: [],              // lines that are fully shown
    prompt: "⇋ ",
    readOnly: false,        // only allow user input if true
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
};

interface CreatTerminalConfig {
    readOnly?: boolean,
    receiveBuffer?: TerminalLine[],
    autoScroll?: boolean,
    renderOutput?: boolean,
}

type TerminalReducer = (state: TerminalState | undefined, action: AnyAction) => TerminalState

export const createTerminalReducer = (id: string, config: CreatTerminalConfig): TerminalReducer => {
    const defaultState = {...terminalStateDefault, ...config, id: id};

    return (terminal: TerminalState | undefined = defaultState , action: AnyAction) => {
        if (action.type === TERMINAL_TICK) {
            return processTick(processTick(processTick(processTick(terminal))));
            // return processTick(terminal);
        }

        // TODO: FIx mess
        if (action.terminalId) {
            if (action.terminalId !== terminal.id) {
                return terminal;
            }
        } else {
            if (!action.data || !action.data.terminalId || action.data.terminalId !== terminal.id) {
                return terminal;
            }
        }

        switch (action.type) {
            case TERMINAL_RECEIVE:
                return receive(terminal, action);
            case SERVER_TERMINAL_RECEIVE:
                return receiveFromServer(terminal, action);
            case TERMINAL_KEY_PRESS:
                return handlePressKey(terminal, action);
            case TERMINAL_SUBMIT:
                return handlePressEnter(terminal, action);
            case SERVER_ERROR:
                return handleServerError(terminal, action);
            case TERMINAL_CLEAR:
                return handleTerminalClear(terminal, action, defaultState);
            case TERMINAL_LOCK:
                return terminalSetReadonly(terminal, action.id, true);
            case TERMINAL_UNLOCK:
                return terminalSetReadonly(terminal, action.id, false);
            case SERVER_TERMINAL_SYNTAX_HIGHLIGHTING:
                return processSyntaxHighlighting(terminal, action.data);
            default:
                return terminal;
        }
    };
};

function processTick(terminal: TerminalState) {
    if (!terminal.receiving) {
        return terminal;
    }
    if (terminal.receivingLine === null && terminal.receiveBuffer.length === 0) {
        return {...terminal, receiving: false};
    }

    let nextReceiveBuffer = [...terminal.receiveBuffer];
    let nextLines = [...terminal.lines];
    let nextRenderingLine: TerminalLine | null = (terminal.renderingLine) ? {...terminal.renderingLine} : {type: "text", data: ""};

    let nextReceivingLine;
    if (terminal.receivingLine !== null) {
        nextReceivingLine = {...terminal.receivingLine};
    } else {
        nextReceivingLine = {...nextReceiveBuffer[0]};
        nextReceiveBuffer = [...terminal.receiveBuffer].splice(1);
    }

    let input = nextReceivingLine.data;

    if (input === "") {
        nextReceivingLine.data = "";
        nextRenderingLine.data = "";
    } else {
        const nextChar = input.substr(0, 1);
        const nextData = input.substr(1);

        nextReceivingLine.data = nextData;
        nextRenderingLine.data += nextChar;
    }

    let nextReceiving = true;
    if (nextReceivingLine.data.length === 0) {
        // finish rendering this line
        nextLines = limitLines([...terminal.lines, nextRenderingLine]);
        nextRenderingLine = null;
        nextReceivingLine = null;
        if (nextReceiveBuffer.length === 0) {
            nextReceiving = false;
        }
    }

    const nextState = {
        ...terminal,
        renderingLine: nextRenderingLine,
        receivingLine: nextReceivingLine,
        lines: nextLines,
        receiving: nextReceiving,
        receiveBuffer: nextReceiveBuffer,
    };
    return nextState;
}


const receive = (terminal: TerminalState, action: AnyAction) => {
    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : [];

    const line = {type: "text", data: action.data};

    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, line],
    };
};

const receiveFromServer = (terminal: TerminalState, action: AnyAction) => {
    const lines = action.data.lines.map((line: TerminalLine) => {
        return {type: "text", data: line}
    });

    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : [];
    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, ...lines],
    }
};

const handlePressKey = (terminal: TerminalState, action: AnyAction): TerminalState => {
    const {keyCode, key} = action;
    if (keyCode === UP || keyCode === DOWN) {
        return handleHistory(terminal, keyCode);
    }

    const newInput = determineInput(terminal.input, keyCode, key);
    return {...terminal, input: newInput}
};

const determineInput = (input: string, keyCode: number, key: string) => {
    if (keyCode === BACKSPACE && input.length > 0) {
        return input.substr(0, input.length - 1);
    }
    if (keyCode === TAB) {
        return input + "[t]";
    }
    if (key.length === 1) {
        return input + key;
    } else {
        return input;
    }
};

const handleHistory = (terminal: TerminalState, keyCode: number): TerminalState => {
    const index = terminal.historyIndex + ((keyCode === UP) ? -1 : 1);
    if (index < 0) {
        return terminal;
    }
    if  (index >= terminal.history.length) {
        return {...terminal, historyIndex: terminal.history.length, input: "" }
    }
    return {...terminal, historyIndex: index, input: terminal.history[index] }

};

const handlePressEnter = (terminal: TerminalState, action: AnyAction): TerminalState => {
    const line = terminal.prompt + action.command;
    const lines = limitLines([...terminal.lines, {type: "text", data: line, class: ["input"]}]);

    const newHistory = limitLines([...terminal.history, (action.command as string)]);

    return {...terminal, lines: lines, input: "", receiving: true, history: newHistory, historyIndex: newHistory.length};
};

/* Only call on mutable array */
const limitLines = (lines: any[]) => {
    while (lines.length > LINE_LIMIT) {
        lines.shift();
    }
    return lines;
};

const handleServerError = (terminal: TerminalState, action: AnyAction) => {
    const retryLines = (action.data.recoverable) ? [
            {type: "text", data: " "},
            {type: "text", data: "[warn b]A server error occurred. Please refresh browser."}]
        : [
            {type: "text", data: " "},
            {type: "text", data: "[warn b]A fatal server error occurred."}];


    const errorLines = [
        {type: "text", data: " "},
        {type: "text", data: "Details: " + action.data.message}
    ];
    return {
        ...terminal,
        input: "",
        receiveInput: null,
        receiveBuffer: [...terminal.receiveBuffer, ...retryLines, ...errorLines],
        receiving: true,
        readOnly: true
    };
};

const handleTerminalClear = (terminal: TerminalState, action: AnyAction, defaultState: TerminalState) => {
    return defaultState;
};

// TODO: remove unused id parameter
const terminalSetReadonly = (terminal: TerminalState, id: string, readOnly: boolean) => {
    return {...terminal, readOnly: readOnly}
};

const processSyntaxHighlighting = (terminal: TerminalState, data: any) => {
    return {...terminal, syntaxHighlighting: data.highlighting}
};
