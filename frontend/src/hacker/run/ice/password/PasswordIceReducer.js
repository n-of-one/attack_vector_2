import {ICE_PASSWORD_SUBMIT, ICE_PASSWORD_UNLOCK, SERVER_ICE_PASSWORD_UPDATE, SERVER_START_HACKING_ICE_PASSWORD} from "./PasswordIceActions";

// const defaultState = {
//     serviceId: "svc-7baa-4572-cde0",
//     nodeId: "node-3933-1233",
//     status: {
//         lockedUntil: 1561909151223,
//         attempts: ["zwaardvis", "secret"],
//         displayHint: false,
//     },
//     locked: false,
// };

const defaultState = {};

const PasswordIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return {...action.data, locked: true};
        case ICE_PASSWORD_UNLOCK:
            return {...state, locked: false};
        case SERVER_ICE_PASSWORD_UPDATE:
            return {...action.data.status, locked: false};
        case ICE_PASSWORD_SUBMIT:
            return {...state, locked: true};
        default:
            return state;
    }
};

export default PasswordIceReducer
