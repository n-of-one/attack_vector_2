import {AnyAction} from "redux";

const themeReducer = (state = "frontier", action: AnyAction) => {
    switch(action.type) {
        default: return state;
    }
};

export default themeReducer
