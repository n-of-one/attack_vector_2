import {SERVER_START_HACKING_ICE_PASSWORD} from "./password/PasswordIceActions";
import {ICE_PASSWORD} from "../../../common/enums/LayerTypes";
import {FINISH_HACKING_ICE} from "../model/HackActions";

const defaultState = {
    layerId: null,
    type: null
};


const CurrentIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return { layerId: action.data.layerId, type: ICE_PASSWORD };
        case FINISH_HACKING_ICE:
            return defaultState;
        default:
            return state;
    }
};

export default CurrentIceReducer
