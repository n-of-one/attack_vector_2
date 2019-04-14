import {TERMINAL_KEY_PRESS, TERMINAL_TICK, TERMINAL_RECEIVE} from "./TerminalActions";

const LINE_LIMIT = 100;

const ENTER_KEY = 13;
const BACKSPACE = 8;
const TAB = 9;


const defaultState = {
    lines: [],
    prompt: "⇋ ",
    readonly: false,
    input: "",
    receiveInput: {type: "text", data: " "},
    receiveBuffer: [{type: "text", data: "[b]🜁 Verdant OS 🜃"}, {type: "text", data: " "}],
    receiveLineIndex: null,
    receiving: true
};


export default (terminal = defaultState, action) => {
    switch (action.type) {
        case TERMINAL_TICK:
            return processTick(processTick(processTick(processTick(terminal))));
        case TERMINAL_RECEIVE:
            return receive(terminal, action);
        case TERMINAL_KEY_PRESS:
            return handleKeyDown(terminal, action);
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

    if (terminal.receiveInput) {
        return {
            ...terminal,
            receiving: true,
            receiveBuffer: [...terminal.receiveBuffer, {type: "text", data: action.data}],
        };
    }
    else {
        return {
            ...terminal,
            receiving: true,
            receiveInput: {type: "text", data: action.data},
        };
    }
};


let handleKeyDown = (terminal, action) => {
    let {keyCode, key} = action;
    if (keyCode === ENTER_KEY) {
        let line = terminal.prompt + terminal.input;
        let lines = limitLines([...terminal.lines, {type: "text", data: line, class: ["input"]}]);
        let receiveBuffer = [...terminal.receiveBuffer, {type: "text", data: "the answer to your action is the following :"}, {
            type: "text",
            data: "..... drummdrummdrummdrummdrummdrummdrummdrummdrumm roll.. 42."
        }];

        return {...terminal, lines: lines, input: "", receiveBuffer: receiveBuffer, receiving: true};
    }

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

/* Only call on mutable array */
let limitLines = (lines) => {
    while (lines.length > LINE_LIMIT) {
        lines.shift();
    }
    return lines;
};