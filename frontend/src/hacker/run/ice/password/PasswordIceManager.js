import {TERMINAL_CLEAR, TERMINAL_RECEIVE} from "../../../../common/terminal/TerminalActions";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {ICE_PASSWORD_BEGIN} from "./PasswordIceActions";
import Schedule from "../../../../common/Schedule";
import {notify} from "../../../../common/Notification";
import {FINISH_HACKING_ICE} from "../../model/HackActions";


class PasswordIceManager {

    init(store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);
    }

    passwordIceStartHack() {
        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});
        this.displayTerminal (20, "↼ Connecting to ice, initiating attack.");
        this.displayTerminal (20, "↼ Scanning for weaknesses.");
        this.displayTerminal (20, "↼ .......................................................................................................................");
        this.displayTerminal (30, "↼ Found weak interface: static (non-rotating) password.");
        this.displayTerminal (20, "↼ Attempting brute force...");
        this.displayTerminal (30, "↺ Detected incremental time-out.");
        this.displayTerminal (20, "↺ Failed to sidestep incremental time-out.");
        this.displayTerminal (20, "");
        this.displayTerminal (20, "↼ Suggested attack vectors: retrieve password, informed password guessing.");
        this.schedule.dispatch(0, {type: ICE_PASSWORD_BEGIN});
    }

    displayTerminal(wait, message) {
        this.schedule.dispatch(wait, {type: TERMINAL_RECEIVE, terminalId: ICE_DISPLAY_TERMINAL_ID, data: message})
    }

    close() {
        this.schedule.clear();
    }

    serverPasswordIceUpdate(action) {
        const currentIce = this.store.getState().run.ice.currentIce;
        if (currentIce.layerId === action.data.layerId) {
            if (action.data.hacked) {
                this.processSuccess(action.data.message);
            } else {
                notify({type: "neutral", title: "Result", message: action.data.message})
            }
        }
    }

    processSuccess(message) {
        notify({type: "ok", title: "Result", message: message});
        this.displayTerminal(0, "");
        this.displayTerminal(20, "Password accepted");
        this.displayTerminal(40, "ICE grants access.");
        this.schedule.dispatch(0, {type: FINISH_HACKING_ICE});
    }



}

const passwordIceManager = new PasswordIceManager();

export default passwordIceManager