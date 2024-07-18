import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {sweeperCanvas} from "./canvas/SweeperCanvas";
import {delay} from "../../../common/util/Util";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {larp} from "../../../common/Larp";
import {SWEEPER_BEGIN} from "./reducer/SweeperUiStateReducer";
import {SweeperEnterData, SweeperModifyAction, SweeperModifyData} from "./SweeperServerActionProcessor";
import {serverCellsToGameCells, serverModifiersToGameModifiers,} from "./SweeperLogic";
import {SweeperCellModifier, SweeperCellType} from "./SweeperModel";
import {ice} from "../../StandaloneGlobals";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {currentUser} from "../../../common/user/CurrentUser";
import {notify} from "../../../common/util/Notification";


class SweeperIceManager extends GenericIceManager {


    cells: SweeperCellType[][] = []
    modifiers: SweeperCellModifier[][] = []
    solved: boolean = false
    userBlocked: boolean = false

    enter(sweeperEnterData: SweeperEnterData) {
        if (sweeperEnterData.hacked) {
            this.enterHacked()
            return
        }

        this.cells = serverCellsToGameCells(sweeperEnterData.cells)
        this.modifiers = serverModifiersToGameModifiers(sweeperEnterData.modifiers)

        this.userBlocked = sweeperEnterData.blockedUserIds.includes(currentUser.id)
        const gameState = {cells: this.cells, modifiers: this.modifiers, strength: sweeperEnterData.strength, hacked: false, userBlocked: this.userBlocked}

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

    serverModified(data: SweeperModifyData) {
        const newModifier = this.newModifier(data.action)
        data.cells.forEach((location: string) => { // "$x:$y"
            const x = parseInt(location.split(":")[0])
            const y = parseInt(location.split(":")[1])
            this.modifiers[y][x] = newModifier
        })
        sweeperCanvas.serverModified(data, newModifier)
    }

    private newModifier(action: SweeperModifyAction): SweeperCellModifier {
        switch (action) {
            case SweeperModifyAction.CLEAR: return SweeperCellModifier.UNREVEALED
            case SweeperModifyAction.FLAG: return SweeperCellModifier.FLAG
            case SweeperModifyAction.QUESTION_MARK: return SweeperCellModifier.QUESTION_MARK
            case SweeperModifyAction.REVEAL:return SweeperCellModifier.REVEALED
            case SweeperModifyAction.EXPLODE: return SweeperCellModifier.REVEALED
        }
    }

    click(x: number, y: number, leftClick: boolean) {
        if (this.solved || this.userBlocked) return
        const modifier = this.modifiers[y][x]
        if (modifier === SweeperCellModifier.REVEALED) {
            return
        }
        const action = this.determineAction(leftClick, modifier)
        const payload = {iceId: ice.id, x, y, action}
        webSocketConnection.send("/ice/sweeper/interact", JSON.stringify(payload))
    }

    private determineAction(leftClick: boolean, modifier: SweeperCellModifier): SweeperModifyAction {
        if (leftClick) return SweeperModifyAction.REVEAL
        if (modifier === SweeperCellModifier.UNREVEALED) return SweeperModifyAction.FLAG
        if (modifier === SweeperCellModifier.FLAG) return SweeperModifyAction.QUESTION_MARK
        if (modifier === SweeperCellModifier.QUESTION_MARK) return SweeperModifyAction.CLEAR
        throw new Error("unexpected state: " + modifier + " leftClick: " + leftClick)
    }

    serverSolved() {
        this.solved = true
        sweeperCanvas.serverSolved()
    }

    blockUser(userId: string) {
        if (currentUser.id === userId) {
            this.userBlocked = true
            notify({message: 'You have been blocked from interacting with the ice', type: 'ok'})
        }
    }
}

export const sweeperIceManager = new SweeperIceManager();
