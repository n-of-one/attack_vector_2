import {HIDDEN, UNLOCKED} from "../IceUiState";
import {ICE_TANGLE_BEGIN, SERVER_START_HACKING_ICE_TANGLE} from "./TangleIceActions";
import {ICE_TANGLE} from "../../../../common/enums/LayerTypes";

const defaultState = {
    strength: "AVERAGE",
    uiState: HIDDEN,
};

const TangleIceReducer = (state = defaultState, action, currentIce) => {
    if (!currentIce ||  currentIce.type !== ICE_TANGLE) {
        return
    }

    switch (action.type) {
        case SERVER_START_HACKING_ICE_TANGLE:
            return { ...action.data, uiState: HIDDEN };
        case ICE_TANGLE_BEGIN:
            return { ...state, uiState: UNLOCKED };
        default:
            return state;
    }
};



export default TangleIceReducer
