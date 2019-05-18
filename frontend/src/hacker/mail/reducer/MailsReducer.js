// import {RECEIVE_SCANS} from "../HackerActions";

const defaultState = [
    {
        id: "mail-1234-2144",
        timestamp: new Date(),
        read: false,
        from: "overlord12",
        title: "Scan",
        text: [
            { type: "text", data: "Welcome to verdant technologies."},
            { type: "text", data: "This is your first email."},
            { type: "text", data: "Sure is exciting!."},
        ]
    }
];

export default (state = defaultState, action) => {
    switch(action.type) {
        default: return state;
    }
}
