import {ICE_PASSWORD_SUBMIT, ICE_PASSWORD_BEGIN, SERVER_ICE_PASSWORD_UPDATE, SERVER_START_HACKING_ICE_PASSWORD, ICE_PASSWORD_LOCK} from "./PasswordIceActions";
import {TERMINAL_TICK} from "../../../../common/terminal/TerminalActions";
import {ICE_PASSWORD} from "../../../../common/enums/ServiceTypes";
import {HIDDEN, LOCKED, UNLOCKED} from "./PasswordIceUiState";

// const defaultState = {
//     serviceId: "svc-7baa-4572-cde0",
//     lockedUntil: "2019-08-26T15:38:40.9179757+02:00",
//     attempts: ["zwaardvis", "secret"],
//     uiState: "UNLOCKED",
//     waitSeconds: 7,
//     hacked: false,
//     hint: "mother's name",
// };

const defaultState = {};

const PasswordIceReducer = (state = defaultState, action, currentIce) => {
    if (!currentIce || currentIce.type !== ICE_PASSWORD) {
        return state;
    }

    switch (action.type) {
        case TERMINAL_TICK: {
            return processTick(state);
        }

        case SERVER_START_HACKING_ICE_PASSWORD:
            return processStartHacking(action.data);
        case SERVER_ICE_PASSWORD_UPDATE:
            return processServerUpdate(action.data, currentIce, state);
        case ICE_PASSWORD_BEGIN:
            return {...state, uiState: UNLOCKED};
        case ICE_PASSWORD_LOCK:
            return {...state, uiState: LOCKED};
        default:
            return state;
    }
};

const processTick = (state) => {
    if (!state.waitSeconds || state.waitSeconds <= 0 ) {
        return state;
    }

    const waitSeconds = calculateWaitSeconds(state);
    return {...state, waitSeconds: waitSeconds, locked: false};
};


const processStartHacking = (serverStatus) => {
    // Start locked while terminal displays flavour text. Unlocks via PasswordIceSagas.passwordIceStartHack

    const waitSeconds = calculateWaitSeconds(serverStatus);
    return {...serverStatus, waitSeconds: waitSeconds, uiState: HIDDEN };
};

const processServerUpdate = (serverStatus, currentIce, oldState) => {
    // disregard updates for password ice that this player is not hacking.
    if (currentIce.serviceId !== serverStatus.serviceId) {
        return oldState;
    }

    const waitSeconds = calculateWaitSeconds(serverStatus);
    return {...serverStatus, waitSeconds: waitSeconds, uiState: UNLOCKED};
};


const calculateWaitSeconds = (serverStatus) => {
    const lockedUntilSeconds = new Date(serverStatus.lockedUntil).getTime();
    return Math.ceil((lockedUntilSeconds - new Date().getTime())/1000);
};



export default PasswordIceReducer
