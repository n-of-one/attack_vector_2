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

const LINE_LIMIT = 100;

const BACKSPACE = 8;
const TAB = 9;


const defaultStateTemplate = {
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
    syntaxHighlighting: { },
};

const createTerminalReducer = (id, config) => {
    const defaultState = {...defaultStateTemplate, ...config, id: id};

    return (terminal = defaultState, action) => {
        if  (action.type === TERMINAL_TICK) {
            return processTick(processTick(processTick(processTick(terminal))));
        }

        if (action.terminalId) {
            if (action.terminalId !== terminal.id) {
                return terminal;
            }
        }
        else {
            if (!action.data || !action.data.terminalId || action.data.terminalId !== terminal.id ) {
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

function processTick(terminal) {
    if (!terminal.receiving) {
        return terminal;
    }
    if (terminal.receivingLine === null && terminal.receiveBuffer.length === 0) {
        return {...terminal, receiving: false};
    }

    let nextReceiveBuffer = [...terminal.receiveBuffer];
    let nextLines = [...terminal.lines];
    let nextRenderingLine = (terminal.renderingLine) ? {...terminal.renderingLine} : {type: "text", data: ""};

    let nextReceivingLine;
    if (terminal.receivingLine !== null) {
        nextReceivingLine = {...terminal.receivingLine };
    }
    else {
        nextReceivingLine = { ...nextReceiveBuffer[0] };
        nextReceiveBuffer = [...terminal.receiveBuffer].splice(1);
    }

    let input = nextReceivingLine.data;

    if (input === "") {
        nextReceivingLine.data = "";
        nextRenderingLine.data = "";
    }
    else {
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


const receive = (terminal, action) => {
    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : [];

    const line = {type: "text", data: action.data};

    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, line],
    };
};

const receiveFromServer = (terminal, action) => {
    const lines = action.data.lines.map((line) => {
        return {type: "text", data: line}
    });

    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : [];
    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, ...lines],
    }
};

const handlePressKey = (terminal, action) => {
    const {keyCode, key} = action;
    const newInput = determineInput(terminal.input, keyCode, key);
    return {...terminal, input: newInput}
};

const determineInput = (input, keyCode, key) => {
    if (keyCode === BACKSPACE && input.length > 0) {
        return input.substr(0, input.length - 1);
    }
    if (keyCode === TAB) {
        return input + "[t]";
    }
    if (key.length === 1) {
        return input + key;
    }
    else {
        return input;
    }
};

const handlePressEnter = (terminal, action) => {
    const line = terminal.prompt + action.command;
    const lines = limitLines([...terminal.lines, {type: "text", data: line, class: ["input"]}]);
    return {...terminal, lines: lines, input: "", receiving: true};
};

/* Only call on mutable array */
const limitLines = (lines) => {
    while (lines.length > LINE_LIMIT) {
        lines.shift();
    }
    return lines;
};

const handleServerError = (terminal, action) => {
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

const handleTerminalClear = (terminal, action, defaultState) => {
        return defaultState;
};

const terminalSetReadonly = (terminal, id, readOnly) => {
    return {...terminal, readOnly: readOnly}
};

const processSyntaxHighlighting = (terminal, data) => {
    return {...terminal, syntaxHighlighting: data}
};


export default createTerminalReducer;