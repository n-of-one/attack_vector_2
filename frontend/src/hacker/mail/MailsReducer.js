// import {RECEIVE_SCANS} from "../HackerActions";

const defaultState = {
    "mail-1234-2144":
        {
            id: "mail-1234-2144",
            timestamp: new Date(),
            read: false,
            from: "overlord12",
            title: "scan for hiveboats-pradza.edu.gov",
            lines: [
                {type: "text", data: "Welcome to verdant technologies."},
                {type: "text", data: "This is your first email."},
                {type: "text", data: "Sure is exciting!."},
            ]
        },
    "mail-2233-fd99":
        {
            id: "mail-2233-fd99",
            timestamp: new Date(),
            read: false,
            from: "system",
            title: "hardware error detected",
            lines: [
                {type: "text", data: "Welcome to verdant technologies."},
                {type: "text", data: "This is your second mail."},
                {type: "text", data: "The thrill of it ;)"},
            ]
        }
};

export default (state = defaultState, action) => {
    switch (action.type) {
        default:
            return state;
    }
}
