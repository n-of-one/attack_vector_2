import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer"
import {Schedule} from "../../../common/Schedule"
import {notify} from "../../../common/Notification"
import {Dispatch, Store} from "redux"
import {GenericIceManager} from "../../GenericIceManager"
import {ICE_PASSWORD_BEGIN} from "./PasswordIceReducer"
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer"
import {terminalManager} from "../../../common/terminal/TerminalManager"

export interface PasswordIceStateUpdate {
    message?: string,
    hacked: boolean,
    hint?: string,
    layerId: string,
    attempts: String[],
    lockedUntil: string
}

class PasswordIceManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    quickPlaying = false

    init(store: Store) {
        this.store = store
        this.dispatch = store.dispatch
        this.schedule = new Schedule(store.dispatch)
    }

    enter() {
        this.schedule.clear()
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        this.displayTerminal(20, "↼ Connecting to ice, initiating attack.")
        if (!this.quickPlaying) {
            this.displayTerminal(20, "↼ Scanning for weaknesses.")
            this.displayTerminal(20, "↼ .......................................................................................................................")
            this.displayTerminal(30, "↼ Found weak interface: static (non-rotating) password.")
            this.displayTerminal(20, "↼ Attempting brute force...")
            this.displayTerminal(30, "↺ Detected incremental time-out.")
            this.displayTerminal(20, "↺ Failed to sidestep incremental time-out.")
            this.displayTerminal(20, "")
        }
        this.displayTerminal(20, "↼ Suggested attack vectors: retrieve password, informed password guessing.")
        this.schedule.dispatch(0, {type: ICE_PASSWORD_BEGIN})
        this.schedule.run(0, () => {
            terminalManager.start()
        })

    }

    close() {
        this.schedule.clear()
    }

    serverPasswordIceUpdate(serverIceState: PasswordIceStateUpdate) {
        if (serverIceState.hacked) {
            this.processSuccess(serverIceState.message!)
        } else {
            notify({type: "neutral", title: "Result", message: serverIceState.message!})
        }
    }

    processSuccess(message: string) {
        notify({type: "ok", title: "Result", message: message})
        this.displayTerminal(0, "")
        this.displayTerminal(20, "Password accepted")
        this.displayTerminal(40, "ICE grants access.")
        this.schedule.run(0, () => {
            window.close()
        })
    }

}

export const passwordIceManager = new PasswordIceManager()
