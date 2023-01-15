import {RECEIVE_SITES} from "./GmActions";

const defaultState = [];

const gmSitesReducer = (state = defaultState, action) => {
    switch(action.type) {
        case RECEIVE_SITES : return receive_sites(action);
        default: return state;
    }
}

const receive_sites = (action) => {
    let sites = action.sites;
    return sites;

};

export default gmSitesReducer;
