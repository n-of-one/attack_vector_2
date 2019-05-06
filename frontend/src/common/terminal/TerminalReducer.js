import {TERMINAL_KEY_PRESS, TERMINAL_TICK, TERMINAL_RECEIVE, TERMINAL_SUBMIT, SERVER_TERMINAL_RECEIVE} from "./TerminalActions";
import {SERVER_FATAL} from "../CommonActions";

const LINE_LIMIT = 100;

const BACKSPACE = 8;
const TAB = 9;


const defaultState = {
    lines: [],
    prompt: "⇋ ",
    readonly: false, // only allow user input if true
    input: "",
    receiveInput: {type: "text", data: " "},
    receiveBuffer: [{type: "text", data: "[b]🜁 Verdant OS 🜃"}, {type: "text", data: " "}],
    receiveLineIndex: null,
    receiving: true,   // true if there is data incoming that is not yet rendered. Effectively this means that there is data in receiveInput and/or receiveBuffer.

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

    if (terminal.receiveLineIndex === null) {
        let receiveLine = {type: "text", data: ""};
        let lines = limitLines([...terminal.lines, receiveLine]);
        let receiveLineIndex = lines.length - 1;
        return {...terminal, lines: lines, receiveLineIndex: receiveLineIndex};
    }

    let input = terminal.receiveInput.data;

    let nextChar = input.substr(0, 1);
    let nextData = input.substr(1);

    let nextReceiveInput = {...terminal.receiveInput, data: nextData};

    let clonedLines = [...terminal.lines];
    let receiveLine = clonedLines[terminal.receiveLineIndex];
    receiveLine.data = receiveLine.data + nextChar;


    let nextReceiving = true;
    let nextReceiveBuffer = terminal.receiveBuffer;
    let nextReceiveLineIndex = terminal.receiveLineIndex;
    if (nextReceiveInput.data.length === 0) {
        if (terminal.receiveBuffer.length === 0) {
            nextReceiving = false;
        }
        else {
            nextReceiveLineIndex = null;
            nextReceiveInput = terminal.receiveBuffer[0];
            nextReceiveBuffer = [...terminal.receiveBuffer].splice(1);
        }
    }

    let nextState = {
        ...terminal,
        receiveInput: nextReceiveInput,
        lines: clonedLines,
        receiving: nextReceiving,
        receiveBuffer: nextReceiveBuffer,
        receiveLineIndex: nextReceiveLineIndex
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
    let a = {
        ...terminal,
        lines: [...terminal.lines, ...terminal.receiveBuffer,  {type: "text", data: "[warn b]" + action.data}],
        input: "",
        receiveInput: [],
        receiveBuffer: [],
        receiving: false,
        readonly: true
    };
    return a;

};
