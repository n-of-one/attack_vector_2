import {AnyAction} from "redux";

export const themeReducer = (state = "frontier", action: AnyAction) => {
    switch(action.type) {
        default: return state;
    }
};
