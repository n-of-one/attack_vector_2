import {TERMINAL_KEY_PRESS, TERMINAL_TICK, TERMINAL_RECEIVE, TERMINAL_SUBMIT, SERVER_TERMINAL_RECEIVE} from "./TerminalActions";
import {SERVER_FATAL} from "../CommonActions";

const LINE_LIMIT = 100;

const BACKSPACE = 8;
const TAB = 9;


const defaultState = {
    lines: [], // lines that are fully shown
    prompt: "⇋ ",
    readonly: false, // only allow user input if true
    input: "", // user input
    renderingLine: null, // part of the current rendering line that is being shown
    receivingLine: null, // part of the current rendering line that is waiting to be shown
    receiveBuffer: [{type: "text", data: "[b]🜁 Verdant OS 🜃"}, {type: "text", data: " "}], // lines waiting to be shown.
    receiving: true,   // true if there are lines waiting to be shown, or in the process of being shown.

};


export default (terminal = defaultState, action) => {

    switch (action.type) {
        case TERMINAL_TICK:
            return processTick(processTick(processTick(processTick(terminal))));
        case TERMINAL_RECEIVE:
            return receive(terminal, action);
        case SERVER_TERMINAL_RECEIVE:
            return receiveFromServer(terminal, action);
        case TERMINAL_KEY_PRESS:
            return handlePressKey(terminal, action);
        case TERMINAL_SUBMIT:
            return handlePressEnter(terminal, action);
        case SERVER_FATAL:
            return handleServerFatal(terminal, action);
        default:
            return terminal;
    }
}

function processTick(terminal) {
    if (!terminal.receiving) {
        return terminal;
    }
    if (terminal.receivingLine === null && terminal.receiveBuffer.length === 0 ) {
        return { ...terminal, receiving: false };
    }

    let renderingLine = (terminal.renderingLine) ? terminal.renderingLine : {type: "text", data: ""};

    let receivingLine = terminal.receivingLine;
    let receiveBuffer = terminal.receiveBuffer;
    if (receivingLine === null) {
        receivingLine = receiveBuffer[0];
        receiveBuffer = [...terminal.receiveBuffer].splice(1);
    }

    let input = receivingLine.data;

    let nextChar = input.substr(0, 1);
    let nextData = input.substr(1);

    receivingLine.data = nextData;
    renderingLine.data += nextChar;


    let lines = terminal.lines;


    let receiving = true;
    if (receivingLine.data.length === 0) {
        // finish rendering this line
        lines = limitLines([...terminal.lines, renderingLine]);
        renderingLine = null;
        receivingLine = null;
        if (receiveBuffer.length === 0) {
            receiving = false;
        }
    }

    let nextState = {
        ...terminal,
        renderingLine: renderingLine,
        receivingLine: receivingLine,
        lines: lines,
        receiving: receiving,
        receiveBuffer: receiveBuffer,
    };
    return nextState;
}


let receive = (terminal, action) => {
    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : [];

    const line = {type: "text", data: action.data};

    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, line],
    };
};

let receiveFromServer = (terminal, action) => {
    const lines = action.data.map((line) => {
        return {type: "text", data: line}
    });

    const buffer = (terminal.receiveBuffer) ? terminal.receiveBuffer : [];
    return {
        ...terminal,
        receiving: true,
        receiveBuffer: [...buffer, ...lines],
    }
};

let handlePressKey = (terminal, action) => {
    let {keyCode, key} = action;
    let newInput = determineInput(terminal.input, keyCode, key);
    return {...terminal, input: newInput}
};

let determineInput = (input, keyCode, key) => {
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

let handlePressEnter = (terminal, action) => {
    let line = terminal.prompt + action.command;
    let lines = limitLines([...terminal.lines, {type: "text", data: line, class: ["input"]}]);
    return {...terminal, lines: lines, input: "", receiving: true};
};

/* Only call on mutable array */
let limitLines = (lines) => {
    while (lines.length > LINE_LIMIT) {
        lines.shift();
    }
    return lines;
};

let handleServerFatal = (terminal, action) => {
    return {
        ...terminal,
        input: "",
        receiveInput: null,
        receiveBuffer: [...terminal.receiveBuffer, {type: "text", data: " "}, {type: "text", data: "[warn b]" + action.data}],
        receiving: true,
        readonly: true
    };
};
