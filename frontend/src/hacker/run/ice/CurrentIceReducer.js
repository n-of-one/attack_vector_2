import {SERVER_START_HACKING_ICE_PASSWORD} from "./password/PasswordIceActions";
import {ICE_PASSWORD} from "../../../common/enums/ServiceTypes";

const defaultState = null;


const CurrentIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return ICE_PASSWORD;
        default:
            return state;
    }
};

export default CurrentIceReducer
