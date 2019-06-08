import {post} from "../common/RestClient";
import {RECEIVE_SITES} from "./GmActions";
import {notify_fatal} from "../common/Notification";


const fetchSites = (dispatch) => {

    post({
        url: "/api/site/",
        body: {},
        ok: (sites) => {
            dispatch({type: RECEIVE_SITES, sites: sites });
        },
        notok: () => {
            notify_fatal("Failed to receive site list from server.");
        }
    });
};

export { fetchSites }