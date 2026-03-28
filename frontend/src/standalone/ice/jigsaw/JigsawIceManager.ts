import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {jigsawCanvas} from "./canvas/JigsawCanvas";
import {delay} from "../../../common/util/Util";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {JIGSAW_BEGIN} from "./reducer/JigsawUiStateReducer";
import {JigsawEnterData} from "./JigsawServerActionProcessor";

class JigsawIceManager extends GenericIceManager {

    solved: boolean = false

    enter(data: JigsawEnterData) {
        if (data.hacked) {
            this.enterHacked()
            return
        }

        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});

        // this.displayTerminal(15, "[primary]↼ Connecting to ice, initiating attack.");
        // this.displayTerminal(40, "↼ Analyzing image structure.");
        // this.displayTerminal(20, "↼ Pattern fragmentation detected.");
        // this.displayTerminal(10, "↼ Exploit [ok]success[/] (error message: [warn]00512[/] block unstable) ");
        // this.displayTerminal(20, "↼ Puzzle interface [info]online");
        this.displayTerminal(0, "↼ Puzzle interface [info]online");
        this.schedule.dispatch(0, {type: JIGSAW_BEGIN});

        delay(() => {
            jigsawCanvas.init(data, this.dispatch, this.store)
        })
    }
}

export const jigsawIceManager = new JigsawIceManager();
