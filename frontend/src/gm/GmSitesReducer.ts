import {RECEIVE_SITES} from "./GmActions";

export interface GmSite { id: string, name: string }

interface ReceiveSites {
    type: string,
    sites: Array<GmSite>
}

const gmSitesReducer = (state: Array<GmSite> = [], action: ReceiveSites) => {
    switch (action.type) {
        case RECEIVE_SITES :
            return action.sites;
        default:
            return state;
    }
}


export default gmSitesReducer;
