import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {sweeperCanvas} from "./canvas/SweeperCanvas";
import {delay} from "../../../common/util/Util";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {SWEEPER_BEGIN, SWEEPER_RESET_START, SWEEPER_RESET_STOP} from "./reducer/SweeperUiStateReducer";
import {SweeperEnterData, SweeperModifyAction, SweeperModifyData} from "./SweeperServerActionProcessor";
import {serverCellsToGameCells, serverModifiersToGameModifiers, SweeperCellModifier, SweeperCellType,} from "./logic/SweeperLogic";
import {ice} from "../../StandaloneGlobals";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {currentUser} from "../../../common/user/CurrentUser";
import {notify} from "../../../common/util/Notification";
import {avEncodedUrl} from "../../../common/util/PathEncodeUtils";


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

        this.userBlocked = sweeperEnterData.blockedUserIds.includes(currentUser.idOrEmptyString())
        const gameState = {
            cells: this.cells,
            modifiers: this.modifiers,
            strength: sweeperEnterData.strength,
            hacked: false,
            blockedUserIds: sweeperEnterData.blockedUserIds,
            minesLeft: sweeperEnterData.minesLeft
        }

        delay(() => {
            sweeperCanvas.init(gameState, this.dispatch, this.store)
        })
        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});
        if (!sweeperEnterData.quickPlaying) {
            this.displayTerminal(15, "[primary]↼ Connecting to ice, initiating attack.");
            this.displayTerminal(40, "↼ Probing for weaknesses.");
            this.displayTerminal(20, "↼ Memory corruption vulnerability detected.");
            this.displayTerminal(10, "↼ Exploit [ok]success[/] (error message: [warn]00387[/] block unstable) ");
            this.displayTerminal(20, "↼ Memory meta access requested.");
            this.displayTerminal(10, "...granted");
        }
        this.displayTerminal(5, "↼ Repair interface [info]online");
        this.schedule.dispatch(0, {type: SWEEPER_BEGIN});
        if (this.modifiers[0][0] !== SweeperCellModifier.REVEALED) {
            this.displayTerminal(10, "");
            this.displayTerminal(10, "↼ Geometric analysis: [info]corner blocks[/] are [ok]safe");
        }

        if (this.userBlocked) {
            this.displayTerminal(10, "");
            this.displayTerminal(5, "↼ [warn]Warning[/] connection blocked due to previous access of corrupted memory.")
            this.displayTerminal(5, "Press (and hold) [b info]reset[/] to restart this hack.");
            this.displayTerminal(5, "");
        }
    }

    serverModified(data: SweeperModifyData) {
        const newModifier = this.newModifier(data.action)
        data.cells.forEach((location: string) => { // "$x:$y"
            const parts =  location.split(":")
            const x = parseInt(parts[0])
            const y = parseInt(parts[1])
            this.modifiers[y][x] = newModifier
        })
        sweeperCanvas.serverModified(data, newModifier)
    }

    private newModifier(action: SweeperModifyAction): SweeperCellModifier {
        switch (action) {
            case SweeperModifyAction.CLEAR: return SweeperCellModifier.HIDDEN
            case SweeperModifyAction.FLAG: return SweeperCellModifier.FLAG
            case SweeperModifyAction.REVEAL:return SweeperCellModifier.REVEALED
            case SweeperModifyAction.EXPLODE: return SweeperCellModifier.REVEALED
            default: throw new Error("unexpected action: " + action)
        }
    }

    click(x: number, y: number, leftClick: boolean) {
        if (this.solved || this.userBlocked) return
        const modifier = this.modifiers[y][x]
        if (modifier === SweeperCellModifier.REVEALED) {
            return // cannot modify revealed cells
        }
        if (leftClick && modifier !== SweeperCellModifier.HIDDEN) {
            return // can only reveal unrevealed cells
        }
        const action = this.determineAction(leftClick, modifier)
        webSocketConnection.send("/ice/sweeper/interact", {iceId: ice.id, x, y, action})
    }

    private determineAction(leftClick: boolean, modifier: SweeperCellModifier): SweeperModifyAction {
        if (leftClick) return SweeperModifyAction.REVEAL
        if (modifier === SweeperCellModifier.HIDDEN) return SweeperModifyAction.FLAG
        if (modifier === SweeperCellModifier.FLAG) return SweeperModifyAction.CLEAR
        throw new Error("unexpected state: " + modifier + " leftClick: " + leftClick)
    }

    serverSolved() {
        this.solved = true
        sweeperCanvas.serverSolved(() => this.processHacked())

        notify({type: "ok", title: "Result", message: "Ice hacked"});
        this.displayTerminal(0, "");
        this.displayTerminal(20, "Memory restored");
        this.displayTerminal(40, "ICE grants access.");
    }

    blockUser(userId: string, userName: string) {
        this.displayTerminal(5, "");
        this.displayTerminal(5, `↼ [warn]Warning[/] corrupted memory accessed, [info]${userName}[/] blocked.`);
        if (currentUser.id === userId) {
            this.userBlocked = true
            notify({message: 'You have been blocked from interacting with the ice', type: 'ok'})
            sweeperCanvas.changeUserBlocked(true)
            this.displayTerminal(5, "Press (and hold) [b info]reset[/] to restart this hack.");
            this.displayTerminal(5, "");
        }
    }

    unblockUser(userId: string, userName: string) {
        this.displayTerminal(5, "");
        this.displayTerminal(5, `↼ Memory access restored for [info]${userName}[/].`);
        if (currentUser.id === userId) {
            this.userBlocked = false
            sweeperCanvas.changeUserBlocked(false)
        }
    }

    startReset(userName: string) {
        this.dispatch({type: SWEEPER_RESET_START})
        this.displayTerminal(5, `↼ Reset initiated by [info]${userName}[/]`);
    }

    stopReset(userName: string) {
        this.dispatch({type: SWEEPER_RESET_STOP})
        this.displayTerminal(5, `↼ Reset [ok]aborted[/]. Keep holding reset to complete the reset.`);
    }

    completeReset(userName: string, newIceId: string) {
        this.displayTerminal(30, `↼ Reset completed by [info]${userName}[/]`);
        this.schedule.run(0, () => {
            const url = avEncodedUrl(`ice/siteHack/${newIceId}`)
            window.open(url, "_self")
        })
    }
}

export const sweeperIceManager = new SweeperIceManager();
