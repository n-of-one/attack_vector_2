import {SERVER_START_HACKING_ICE_PASSWORD} from "./password/PasswordIceActions";
import {ICE_PASSWORD, ICE_TANGLE} from "../../../common/enums/LayerTypes";
import {FINISH_HACKING_ICE} from "../model/HackActions";
import {SERVER_START_HACKING_ICE_TANGLE} from "./tangle/TangleIceActions";

const defaultState = {
    layerId: null,
    type: null
};


const CurrentIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return { layerId: action.data.layerId, type: ICE_PASSWORD };
        case SERVER_START_HACKING_ICE_TANGLE:
            return { layerId: action.data.layerId, type: ICE_TANGLE };
        case FINISH_HACKING_ICE:
            return defaultState;
        default:
            return state;
    }
};

export default CurrentIceReducer
