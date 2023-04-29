import {Schedule} from "../../../common/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {Dispatch, Store} from "redux";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {slowIceCanvas} from "../canvas/SlowIceCanvas";


class SlowIceManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    quickPlaying = false

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);
    }

    // enter(iceId: string, data: ServerEnterIceNetwalk) {
    enter(iceId: string) {
        slowIceCanvas.init(iceId, { totalUnits: 324000,unitsHacked: 100,}, this.store)

        this.schedule.clear()
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        // this.quickPlaying = true

        if (!this.quickPlaying) {
            this.displayTerminal(20, "[warn]↼ Connecting to ice, initiating attack.");
            this.displayTerminal(42, "↼ Detected [info]Dahana[/] network obfuscation.");
            this.displayTerminal(6, "↼ Attempting automatic reconfiguration.");
            this.displayTerminal(34, "↼ Reconfiguring.");
            this.displayTerminal(50, "↺ Status check");
            this.displayTerminal(20, "↼ Network integrity [info]17%");
        }

        // this.schedule.dispatch(0, {type: NETWALK_BEGIN});

        this.displayTerminal(15, "[i primary]↼ Remote core dump analysis session started.");
        this.displayTerminal(5, " ");


    }

    // serverSentNodeRotated(data: NetwalkRotateUpdate) {
    //     netwalkCanvas.serverSentNodeRotated(data)
    //
    //     if (data.hacked) {
    //         netwalkCanvas.finish()
    //         this.displayTerminal(10, "⇁ Configuration [ok]restored");
    //         this.displayTerminal(44, "");
    //         this.displayTerminal(50, "[warn]↼ Access granted.");
    //         this.displayTerminal(20, "");
    //
    //         this.schedule.run(0, () => {
    //             window.close()
    //         });
    //     }
    // }
}

export const slowIceManager = new SlowIceManager();
