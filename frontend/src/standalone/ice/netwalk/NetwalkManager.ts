import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {netwalkCanvas} from "./canvas/NetwalkCanvas";
import {NetwalkRotateUpdate, ServerEnterIceNetwalk} from "./NetwalkServerActionProcessor";
import {NETWALK_BEGIN} from "./reducer/NetwalkStateReducer";


class NetwalkManager extends GenericIceManager {


    enter(data: ServerEnterIceNetwalk) {
        netwalkCanvas.init(data, this.dispatch, this.store)

        this.schedule.clear()
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        if (data.hacked) {
            this.enterHacked()
            return
        }

        if (!data.quickPlaying) {
            this.displayTerminal(20, "[warn]↼ Connecting to ice, initiating attack.");
            this.displayTerminal(42, "↼ Detected [info]Sanrachana[/] network obfuscation.");
            this.displayTerminal(6, "↼ Attempting automatic reconfiguration.");
            this.displayTerminal(34, "↼ Reconfiguring.");
            this.displayTerminal(50, "↺ Status check");
            this.displayTerminal(20, "↼ Network integrity [info]17%");
        }

        this.schedule.dispatch(0, {type: NETWALK_BEGIN});

        this.displayTerminal(15, "[i primary]↼ Remote core dump analysis session started.");
        this.displayTerminal(5, " ");
    }

    serverSentNodeRotated(data: NetwalkRotateUpdate) {
        netwalkCanvas.serverSentNodeRotated(data)

        if (!data.hacked) {
            return
        }
        netwalkCanvas.finish()
        this.displayTerminal(10, "⇁ Configuration [ok]restored");
        this.displayTerminal(44, "");
        this.displayTerminal(50, "[warn]↼ Access granted.");
        this.displayTerminal(20, "");

        this.processHacked()
    }
}

export const netwalkManager = new NetwalkManager();
