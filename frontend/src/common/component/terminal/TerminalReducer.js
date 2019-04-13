import {TERMINAL_KEY_PRESS, TERMINAL_TICK} from "./TerminalActions";

const ENTER_KEY = 13;
const BACKSPACE = 8;
const TAB = 9;

const defaultState = {
    lines: [{type: "text", data: "line1"}, {type: "text", data: "line2"}],
    prompt: "> ",
    readonly: false,
    input: "",
    receiveInput: {type: "text", data: "Helllo this is slowly received"}, // The current receiveing line
    receiveBuffer: [{type: "text", data: "mooore data to receive"}, {type: "text", data: "and even more!!"}],
    receiveLineIndex: null,
    receiving: true
};

function processTick(terminal) {
    if (!terminal.receiving) {
        return terminal;
    }
    
    if (terminal.receiveLineIndex === null) {
        let receiveLine = { type: "text", data: ""};
        let lines = [ ...terminal.lines, receiveLine ];
        let receiveLineIndex = lines.length -1;
        return { ...terminal, lines: lines, receiveLineIndex: receiveLineIndex };
    }

    let input = terminal.receiveInput.data;

    let nextChar = input.substr(0, 1);
    let nextData = input.substr(1);
    
    let nextReceiveInput = {...terminal.receiveInput, data: nextData };

    let clonedLines = [...terminal.lines];
    let receiveLine = clonedLines[terminal.receiveLineIndex];
    clonedLines[terminal.receiveLineIndex] = { ...receiveLine, data: receiveLine.data + nextChar };


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
            nextReceiveBuffer = [ ...terminal.receiveBuffer ].splice(1);
        }
    }

    let nextState = { ... terminal,
        receiveInput: nextReceiveInput,
        lines: clonedLines,
        receiving: nextReceiving,
        receiveBuffer: nextReceiveBuffer,
        receiveLineIndex: nextReceiveLineIndex
    };
    return nextState;
}

export default (terminal = defaultState, action) => {
    switch(action.type) {
        case TERMINAL_TICK: return processTick(terminal);
        case "ADD_LINE": return addLine(terminal, action);
        case TERMINAL_KEY_PRESS: return handleKeyDown(terminal, action);
        default: return terminal;
    }
}

let addLine = (terminal, action) => {
    return {
        ...terminal,
        lines: [... terminal.lines, {type: "text", data: "bonus line "}],
    };
};


let handleKeyDown = (terminal, action) => {
    let {keyCode, key} = action;
    if (keyCode === ENTER_KEY) {
        let line = terminal.prompt + terminal.input;
        let lines = [ ...terminal.lines, {type: "text", data: line} ];
        return { ...terminal, lines: lines, input: "" }
    }

    let newInput = determineInput(terminal.input, keyCode, key);
    return { ...terminal, input: newInput }
};


let determineInput = (input, keyCode, key) => {
    if (keyCode === BACKSPACE && input.length > 0) {
        return input.substr(0, input.length-1);
    }
    if (keyCode === TAB ) {
        return input + "[t]";
    }
    if (key.length === 1) {
        return input + key;
    }
    else {
        return input;
    }
};