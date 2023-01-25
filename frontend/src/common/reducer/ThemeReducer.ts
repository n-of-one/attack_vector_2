import {AnyAction} from "redux";

export const themeReducer = (state = "frontier", action: AnyAction): string => {
    switch(action.type) {
        default: return state;
    }
};
