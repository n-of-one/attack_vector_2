const defaultState = {
    lines: [{type: "text", data: "line1"}, {type: "text", data: "line2"}],
    prompt: ">",
    readonly: false,
};

export default (state = defaultState, action) => {
    switch(action.type) {
        default: return state;
    }
}
