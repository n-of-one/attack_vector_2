import {Schedule} from "../../../common/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {Dispatch, Store} from "redux";
import {NetwalkRootState} from "../reducer/NetwlakRootReducer";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {netwalkCanvas} from "../canvas/NetwalkCanvas";
import {NetwalkRotateUpdate, ServerEnterIceNetwalk} from "../NetwalkServerActionProcessor";


class NetwalkManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    quickPlaying = false

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);
    }


    enter(iceId: string, data: ServerEnterIceNetwalk) {
        netwalkCanvas.init(iceId, data, this.dispatch, this.store)

        this.schedule.clear()
        this.dispatch( {type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        this.displayTerminal(10,"[warn]↼ Connecting to ice, initiating attack.");
        if (!this.quickPlaying) {
            this.displayTerminal(10, "⇁ Connection established - authorization handshake started");
            this.displayTerminal(3, "⇁ remote_core_dump_analyze [ok]trusted");
            this.displayTerminal(15, " ");
        }

        // this.schedule.dispatch(0, {type: NETWALK_BEGIN});


        this.displayTerminal(15, "[i primary]↼ Remote core dump analysis session started.");
        this.displayTerminal(5, " ");
    }


    serverSentNodeRotated(data: NetwalkRotateUpdate) {
        netwalkCanvas.serverSentNodeRotated(data)

        if (data.hacked) {
            const rootState = this.store.getState() as NetwalkRootState

            this.schedule.wait(25)

            this.displayTerminal(0, "");
            this.displayTerminal(20, "Blah blah. Status: [strong ok]success");
            this.displayTerminal(0, "");
            this.displayTerminal(40, "[i primary]↼ Ice restored, access granted.");
            this.schedule.run(0, () => {
                window.close()
            });
        }


    }
}

export const netwalkManager = new NetwalkManager();
