import {Schedule} from "../../../common/util/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {Dispatch, Store} from "redux";
import {TERMINAL_CLEAR, TERMINAL_REPLACE_LAST_LINE} from "../../../common/terminal/TerminalReducer";
import {tarCanvas} from "../canvas/TarCanvas";
import {TarEnter, TarStatusUpdate} from "../TarServerActionProcessor";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {formatTimeInterval} from "../../../common/util/Util";
import {TAR_BEGIN} from "../reducer/TarReducer";

const DELAY_BETWEEN_HACK_TICKS_S = 5

class TarManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    quickPlaying = false

    iceId: string = ""

    hackingIntervalId: ReturnType<typeof setInterval> | null = null
    reportingIntervalId: ReturnType<typeof setInterval> | null = null

    totalUnits = -1
    unitsHacked = -1
    lastUnitsHacked = -1

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);
    }

    enter(iceId: string, data: TarEnter) {
        this.iceId = iceId
        this.unitsHacked = data.unitsHacked
        this.lastUnitsHacked = data.unitsHacked
        this.totalUnits = data.totalUnits

        tarCanvas.init(iceId, data, this.store)

        this.schedule.clear()
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        if (!this.quickPlaying) {
            this.displayTerminal(20, "[warn]↼ Connecting to ice, initiating attack.")
            this.displayTerminal(42, "↼ Analysing [info]Tar[/] elliptic curve encryption.")
            this.displayTerminal(6, `↼ ECC strength: [warn]${data.totalUnits}[/] units.`)
            this.displayTerminal(5, "")
            this.displayTerminal(15, "↼ Initializing curve approximation arrays.")
            this.displayTerminal(20, "↼ Generating block matrix.")
        }

        this.schedule.dispatch(35, {type: TAR_BEGIN})

        this.displayTerminal(25, "↼ Matrix status [ok]ready[/].")
        this.displayTerminal(5, "")
        this.displayTerminal(15, "↼ [i]Automatic brute force in progress.")
        this.displayTerminal(20, `↼ Updating progress every [primary]${DELAY_BETWEEN_HACK_TICKS_S}[/] seconds.`)
        this.displayTerminal(0, "")
        this.displayTerminal(0, "")
        this.schedule.run(10, () => {
            this.hackingIntervalId = setInterval(() => {
                const random = Math.floor(Math.random() * 11) - 5
                const unitsHacked = data.unitsPerSecond * DELAY_BETWEEN_HACK_TICKS_S + random

                webSocketConnection.sendObject("/av/ice/tar/hackedUnits", {
                    iceId: this.iceId, units: unitsHacked
                })
            }, DELAY_BETWEEN_HACK_TICKS_S * 1000)
        })
        this.schedule.run(10, () => {
            this.reportingIntervalId = setInterval(() => {
                const progress = this.unitsHacked - this.lastUnitsHacked
                const unitsLeft = this.totalUnits - this.unitsHacked
                const speed = Math.floor(progress / DELAY_BETWEEN_HACK_TICKS_S)
                this.lastUnitsHacked = this.unitsHacked
                const secondsLeft = Math.floor(unitsLeft / speed)

                const timeLeft = formatTimeInterval(secondsLeft)
                this.schedule.dispatch(0, {type: TERMINAL_REPLACE_LAST_LINE, terminalId: ICE_DISPLAY_TERMINAL_ID, data: `Speed: [info]${speed}[/] units/s. To go: ${timeLeft}.`})

            }, DELAY_BETWEEN_HACK_TICKS_S * 1000)
        })
    }


    serverSentUpdate(data: TarStatusUpdate) {
        tarCanvas.update(data)
        this.unitsHacked = data.unitsHacked

        if (data.hacked) {
            tarCanvas.finish()
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

export const tarManager = new TarManager();
