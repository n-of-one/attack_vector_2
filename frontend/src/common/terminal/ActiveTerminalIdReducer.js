import {CHANGE_ACTIVE_TERMINAL} from "./TerminalActions";

// const defaultState = "main";
const defaultState = "iceInput";

const activeTerminalIdReducer = (state = defaultState, action) => {
    switch (action.type) {
        case CHANGE_ACTIVE_TERMINAL:
            return action;
        default:
            return state;
    }
};


export default activeTerminalIdReducer
