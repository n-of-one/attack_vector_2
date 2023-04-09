import {Schedule} from "../../../common/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {notify} from "../../../common/Notification";
import {delay} from "../../../common/Util";
import {Dispatch, Store} from "redux";
import {WORD_SEARCH_BEGIN} from "../reducer/WordSearchStateReducer";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";


class WordSearchManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);

        setTimeout( () => {
            wordSearchCanvas.init("", {}, this.dispatch, this.store)

            this.schedule.clear();
            this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, ".");
            this.displayTerminal(0, "Search fragment: VIRUS");


        }, 100)

    }



    enter(iceId: string) {
        // delay (() => {tangleIceCanvas.init(iceId, puzzle, this.dispatch, this.store)});
        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});
        this.displayTerminal(20, "↼ Connecting to ice, initiating attack.");
        this.displayTerminal(40, "↼ Network inspection.");
        this.displayTerminal(10, "↼ Complete");
        this.displayTerminal(10, "↼ Weak encryption detected: Directed Acyclic Graph");
        this.displayTerminal(20, "↼ Negotiating lowest entropy");
        this.displayTerminal(30, "");
        this.displayTerminal(20, "↼ Negotiation complete.");
        this.displayTerminal(5, "↼ Start manual decryption");
        this.schedule.dispatch(0, {type: WORD_SEARCH_BEGIN});
    }



}

export const tangleIceManager = new WordSearchManager();
