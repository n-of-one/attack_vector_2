import {HIDDEN, UNLOCKED} from "../IceUiState";
import untangleCanvas from "./TangleIceCanvas";
import {ICE_TANGLE} from "../../../../common/enums/LayerTypes";
import {ICE_TANGLE_BEGIN, SERVER_START_HACKING_ICE_TANGLE} from "./TangleIceActions";

const defaultState = {
    uiState: HIDDEN,
};


const TangleIceReducer = (state = defaultState, action, currentIce) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_TANGLE:
            return { ...action.data, uiState: HIDDEN };
        case ICE_TANGLE_BEGIN:
            return { ...state, uiState: UNLOCKED };
        // case SERVER_START_HACKING_ICE_PASSWORD:
        //     return processStartHacking(action.data);
        // case SERVER_ICE_PASSWORD_UPDATE:
        //     return processServerUpdate(action.data, currentIce, state);
        // case ICE_PASSWORD_BEGIN:
        //     return {...state, uiState: UNLOCKED};
        // case ICE_PASSWORD_LOCK:
        //     return {...state, uiState: LOCKED};
        // case FINISH_HACKING_ICE:
        //     return defaultState;
        default:
            return state;
    }

};
//
// const processTick = (state) => {
//     if (!state.waitSeconds || state.waitSeconds <= 0 ) {
//         return state;
//     }
//
//     const waitSeconds = calculateWaitSeconds(state);
//     return {...state, waitSeconds: waitSeconds, locked: false};
// };
//
//
// const processStartHacking = (serverStatus) => {
//     const waitSeconds = calculateWaitSeconds(serverStatus);
//     return {...serverStatus, waitSeconds: waitSeconds, uiState: HIDDEN };
// };
//
// const processServerUpdate = (serverStatus, currentIce, oldState) => {
//     // disregard updates for password ice that this player is not hacking.
//     if (currentIce.layerId !== serverStatus.layerId) {
//         return oldState;
//     }
//
//     const waitSeconds = calculateWaitSeconds(serverStatus);
//     return {...serverStatus, waitSeconds: waitSeconds, uiState: UNLOCKED};
// };
//
//
// const calculateWaitSeconds = (serverStatus) => {
//     const lockedUntilSeconds = new Date(serverStatus.lockedUntil).getTime();
//     return Math.ceil((lockedUntilSeconds - new Date().getTime())/1000);
// };
//


export default TangleIceReducer
