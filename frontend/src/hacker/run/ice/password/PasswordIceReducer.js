import {ICE_PASSWORD_SUBMIT, ICE_PASSWORD_UNLOCK, SERVER_ICE_PASSWORD_UPDATE, SERVER_START_HACKING_ICE_PASSWORD} from "./PasswordIceActions";
import {TERMINAL_TICK} from "../../../../common/terminal/TerminalActions";

// const defaultState = {
//     serviceId: "svc-7baa-4572-cde0",
//     nodeId: "node-3933-1233",
//     status: {
//         lockedUntil: "2019-08-26T15:38:40.9179757+02:00",
//         attempts: ["zwaardvis", "secret"],
//     },
//     locked: false,
//     waitSeconds: 7,
//     hacked: false,
//     hint: "mother's name",
// };

const defaultState = {};

const PasswordIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case TERMINAL_TICK: {
            return processTick(state);
        }
        case SERVER_START_HACKING_ICE_PASSWORD:
            return processStartHacking(action.data);
        case SERVER_ICE_PASSWORD_UPDATE:
            return processServerUpdate(action.data);
        case ICE_PASSWORD_UNLOCK:
            return {...state, locked: false};
        case ICE_PASSWORD_SUBMIT:
            return {...state, locked: true};
        default:
            return state;
    }
};

const processTick = (state) => {
    if (!state.waitSeconds || state.waitSeconds <= 0 ) {
        return state;
    }

    return processServerUpdate(state);
};


const processStartHacking = (serverStatus) => {
    // Start locked while terminal displays flavour text. Unlocks via PasswordIceSagas.passwordIceStartHack
    return {...processServerUpdate(serverStatus), locked: true};
};

const processServerUpdate = (serverStatus) => {
    const waitSeconds = calculateWaitSeconds(serverStatus);
    return {...serverStatus, waitSeconds: waitSeconds, locked: false};
};

const calculateWaitSeconds = (serverStatus) => {
    const lockedUntilSeconds = new Date(serverStatus.status.lockedUntil).getTime();
    return Math.ceil((lockedUntilSeconds - new Date().getTime())/1000);
};



export default PasswordIceReducer
