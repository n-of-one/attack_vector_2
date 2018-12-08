export default (state = "loading", action) => {
    switch(action.type) {
        // case "INIT_STATE" : return fromQuery();
        default: return state;
    }
}
