import {ICE_PASSWORD_SUBMIT, SERVER_ICE_PASSWORD_UPDATE, SERVER_START_HACKING_ICE_PASSWORD} from "./PasswordIceActions";

// const defaultState = "main";
const defaultState = {
    serviceId: "svc-7baa-4572-cde0",
    nodeId: "node-3933-1233",
    status: {
        lockedUntil: 1561909151223,
        attempts: ["zwaardvis", "secret"],
        displayHint: false,
    },
    lockedAfterSubmit: false,
};

const PasswordIceReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return { ...action.data, lockedAfterSubmit: false};
        case SERVER_ICE_PASSWORD_UPDATE:
            return { ...action.data , lockedAfterSubmit: false};
        case ICE_PASSWORD_SUBMIT:
            return { ...state, lockedAfterSubmit: true};
        default:
            return state;
    }
};

export default PasswordIceReducer
