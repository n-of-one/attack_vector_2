import {Schedule} from "../../../common/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {Dispatch, Store} from "redux";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {slowIceCanvas} from "../canvas/SlowIceCanvas";
import {SlowIceEnter, SlowIceStatusUpdate} from "../SlowIceServerActionProcessor";
import {SLOW_ICE_BEGIN} from "../reducer/SlowIceReducer";
import {webSocketConnection} from "../../../common/WebSocketConnection";

const DELAY_BETWEEN_HACK_TICKS_S = 5

class SlowIceManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    quickPlaying = false
    unitsPerSecond = 0

    iceId: string = ""

    hackingIntervalId: ReturnType<typeof setInterval> | null = null

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);
    }

    enter(iceId: string, data: SlowIceEnter) {
        this.iceId = iceId
        this.unitsPerSecond = data.unitsPerSecond

        slowIceCanvas.init(iceId, data, this.store)

        this.schedule.clear()
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        if (!this.quickPlaying) {
            this.displayTerminal(20, "[warn]↼ Connecting to ice, initiating attack.")
            this.displayTerminal(42, "↼ Analysing [info]Taar[/] elliptic curve encryption.")
            this.displayTerminal(6, `↼ ECC strength: [warn]${data.totalUnits}[/].`)
            this.displayTerminal(5, "")
            this.displayTerminal(15, "↼ Initializing curve approximation arrays.")
            this.displayTerminal(20, "↼ Generating block matrix.")
        }

        this.schedule.dispatch(35, {type: SLOW_ICE_BEGIN})

        this.displayTerminal(25, "↼ Matrix status [ok]ready[/].")
        this.displayTerminal(5, "")
        this.displayTerminal(15, "↼ [i]Automatic brute force in progress.")
        this.displayTerminal(20, `↼ Updating progress every [primary]${DELAY_BETWEEN_HACK_TICKS_S}[/] seconds.`)
        this.schedule.run(0, () => {
            this.hackingIntervalId = setInterval(() => {
                webSocketConnection.sendObject("/av/ice/slowIce/hackedUnits", {
                    iceId: this.iceId, units: data.unitsPerSecond * DELAY_BETWEEN_HACK_TICKS_S
                })
            }, DELAY_BETWEEN_HACK_TICKS_S * 1000)
        })
    }


    serverSentUpdate(data: SlowIceStatusUpdate) {
        slowIceCanvas.update(data)

        if (data.hacked) {
            slowIceCanvas.finish()
            this.displayTerminal(30, "");
            this.displayTerminal(50, "[warn]↼ Access granted.");

            if (this.hackingIntervalId) {
                clearInterval(this.hackingIntervalId)
            }
            if (!this.quickPlaying) {
                this.schedule.run(0, () => {
                    window.close()
                })
            }
        }
    }
}

export const slowIceManager = new SlowIceManager();
