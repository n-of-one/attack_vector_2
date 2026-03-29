import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {JigsawCanvas} from "./canvas/JigsawCanvas";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {JIGSAW_BEGIN} from "./reducer/JigsawUiStateReducer";
import {JigsawEnterData} from "./JigsawServerActionProcessor";
import {sourceImageLoaded} from "./component/JigsawHome";

class JigsawIceManager extends GenericIceManager {

    solved: boolean = false

    jigsawCanvas: JigsawCanvas | null = null

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

        sourceImageLoaded.then((sourceImage) => {
            this.jigsawCanvas = new JigsawCanvas(data, this.dispatch, this.store, sourceImage)
        })
    }
}

export const jigsawIceManager = new JigsawIceManager();
