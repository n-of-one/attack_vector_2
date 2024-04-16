import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {sweeperCanvas} from "./canvas/SweeperCanvas";
import {delay} from "../../../common/util/Util";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {larp} from "../../../common/Larp";
import {SWEEPER_BEGIN} from "./reducer/SweeperUiStateReducer";
import {SweeperEnterData} from "./SweeperServerActionProcessor";
import {
    serverGridToGameStateGrid,
    serverGridToModifiers,
} from "./SweeperLogic";
import {SweeperCellModifier} from "./SweeperModel";


class SweeperIceManager extends GenericIceManager {

    enter(sweeperEnterData: SweeperEnterData) {
        if (sweeperEnterData.hacked) {
            this.enterHacked()
            return
        }

        const cells = serverGridToGameStateGrid(sweeperEnterData.grid)
        const modifiers: SweeperCellModifier[][] = serverGridToModifiers(sweeperEnterData.grid)

        const gameState = {cells, modifiers, strength: sweeperEnterData.strength, hacked: false}

        delay(() => {
            sweeperCanvas.init(gameState, this.dispatch, this.store)
        })
        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});
        if (!larp.quickPlaying) {
            this.displayTerminal(20, "↼ Connecting to ice, initiating attack.");
            this.displayTerminal(40, "↼ Network inspection.");
            this.displayTerminal(10, "↼ Complete");
            this.displayTerminal(10, "↼ Weak encryption detected: Undirected Cyclic Graph");
            this.displayTerminal(20, "↼ Negotiating lowest entropy");
            this.displayTerminal(30, "");
            this.displayTerminal(20, "↼ Negotiation complete.");
            this.displayTerminal(5, "↼ Start manual decryption");
        }
        this.schedule.dispatch(0, {type: SWEEPER_BEGIN});
    }

    // moved(data: TanglePointMoved) {
    //
    //     // state.run.ice.currentIce;
    //     sweeperCanvas.serverMovedPoint(data);
    //     if (!data.solved) {
    //         return
    //     }
    //     notify({type: "ok", title: "Result", message: "Ice hacked"});
    //     this.displayTerminal(0, "");
    //     this.displayTerminal(20, "Decryption complete");
    //     this.displayTerminal(40, "ICE grants access.");
    //
    //     this.processHacked()
    // }

}

export const sweeperIceManager = new SweeperIceManager();
