import Schedule from "../../../../common/Schedule";
import {TERMINAL_CLEAR} from "../../../../common/terminal/TerminalActions";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import GenericIceManager from "../GenericIceManager";
import {ICE_TANGLE_BEGIN} from "./TangleIceActions";
import tangleIceCanvas from "./TangleIceCanvas";

class TangleIceManager extends GenericIceManager {

    init(store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);
    }

    startHack(data) {
        tangleIceCanvas.init(data, this.dispatch);
        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});
        // this.displayTerminal (20, "↼ Connecting to ice, initiating attack.");
        // this.displayTerminal (40, "↼ Network inspection.");
        // this.displayTerminal (10, "↼ Complete");
        // this.displayTerminal (10, "↼ Weak encryption detected: Directed Acyclic Graph");
        // this.displayTerminal (20, "↼ Negotiating lowest entropy");
        // this.displayTerminal (30, "");
        // this.displayTerminal (20, "↼ Negotiation complete.");
        this.displayTerminal (5, "↼ Start manual decryption");
        this.schedule.dispatch(0, {type: ICE_TANGLE_BEGIN});
    }

}

const tangleIceManager = new TangleIceManager();
export default tangleIceManager