import {SERVER_START_HACKING_ICE_PASSWORD} from "./password/PasswordIceActions";
import {ICE_PASSWORD} from "../../../common/enums/ServiceTypes";
import {FINISH_HACKING_ICE} from "../model/HackActions";

const defaultState = {
    serviceId: null,
    type: null
};


const CurrentIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return { serviceId: action.data.status.serviceId, type: ICE_PASSWORD };
        case FINISH_HACKING_ICE:
            return defaultState;
        default:
            return state;
    }
};

export default CurrentIceReducer
