import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {tangleIceCanvas} from "./canvas/TangleIceCanvas";
import {notify} from "../../../common/util/Notification";
import {delay} from "../../../common/util/Util";
import {ICE_TANGLE_BEGIN, TangleLine, TanglePoint} from "./reducer/TangleIceReducer";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";

export interface EnterTanglePuzzle {
    strength: string,
    points: TanglePoint[],
    lines: TangleLine[],
    clusters: Number,
    hacked: boolean,
    quickPlaying: boolean,
}

export interface TanglePointMoved {
    layerId: string,
    id: string,
    x: number,
    y: number,
    solved: boolean
}

class TangleIceManager extends GenericIceManager {

    enter(puzzle: EnterTanglePuzzle) {
        if (puzzle.hacked) {
            this.enterHacked()
            return
        }


        delay(() => {
            tangleIceCanvas.init(puzzle, this.dispatch, this.store)
        })
        this.schedule.clear()
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})
        if (!puzzle.quickPlaying) {
            this.displayTerminal(20, "[primary]↼ Connecting to ice, initiating attack")
            this.displayTerminal(40, "↼ Network inspection")
            this.displayTerminal(10, "↼ Complete")
            this.displayTerminal(10, "↼ Weak encryption detected: [info]Undirected Cyclic Graph")
            if (puzzle.clusters > 1) {
                this.displayTerminal(10, `↼ Detected: [info]${puzzle.clusters}[/] clusters`)

            }
            this.displayTerminal(20, "↼ Negotiating lowest entropy")
            this.displayTerminal(30, "...")
            this.displayTerminal(20, "↼ Negotiation complete")
        }
        this.displayTerminal(5, "[primary]↼ Start manual decryption")
        this.schedule.dispatch(0, {type: ICE_TANGLE_BEGIN})
    }

    moved(data: TanglePointMoved) {
        tangleIceCanvas.serverMovedPoint(data)
    }

    serverSentIceHacked() {
        notify({type: "ok", title: "Result", message: "Ice hacked"})
        this.displayTerminal(0, "")
        this.displayTerminal(20, "Decryption complete")
        this.displayTerminal(40, "ICE grants access.")

        this.processHacked()
    }

}

export const tangleIceManager = new TangleIceManager()
