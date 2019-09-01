import {UNLOCKED} from "../IceUiState";
import untangleCanvas from "./TangleCanvas";

// const defaultState = {
//     layerId: "svc-7baa-4572-cde0",
//     lockedUntil: "2019-08-26T15:38:40.9179757+02:00",
//     attempts: ["zwaardvis", "secret"],
//     uiState: "UNLOCKED",
//     waitSeconds: 7,
//     hacked: false,
//     hint: "mother's name",
// };
const defaultState = {
    uiState: UNLOCKED,
};

// FIXME
var init = false;

const PasswordIceReducer = (state = defaultState, action, currentIce) => {
    // FIXME
    // if (!currentIce || currentIce.type !== ICE_UNTANGLE) {
    //     return state;
    // }

    if (!init) {
        setTimeout(() => {
            untangleCanvas.init();
        }, 100);
        init = true;
    }

    switch (action.type) {

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


export default PasswordIceReducer
