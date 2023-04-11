import {Schedule} from "../../../common/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {notify} from "../../../common/Notification";
import {Dispatch, Store} from "redux";
import {WORD_SEARCH_BEGIN} from "../reducer/WordSearchStateReducer";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";
import {ServerEnterIceWordSearch} from "../WordSearchServerActionProcessor";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";


class WordSearchManager extends GenericIceManager {

    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    schedule: Schedule = null as unknown as Schedule

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);


    }


    enter(iceId: string, data: ServerEnterIceWordSearch) {
        setTimeout(() => {
            wordSearchCanvas.init(iceId, {}, this.dispatch, this.store)
        }, 100)

        this.schedule.clear()
        this.dispatch( {type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID})

        // this.displayTerminal(10,"[[i;#a9443b;]↼ Connecting to ice, initiating attack.]");
        //
        // this.displayTerminal(10, "⇁ Connection established - authorization handshake started");
        // this.displayTerminal(8, "⇁ G*G Security token not found in request, fallback to rp/inner/program authorization scheme.");
        // this.displayTerminal(45, '⇁ Accessing rp/i/p client header: [[;#31708f;]client_id=386422_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _' +
        //     '_inject{@cursor(4block rel#00.18), text("____inject{@authorize(92),@c_override("header/2", "trusted"), @remote_core_dump_analyze()}___"), @marker(0),' +
        //     '@allocate_mem(999 4blocks), @jump(#marker(0))} _ _ ]');
        // this.displayTerminal(0, "⇁ Memory allocation overflow, core dump");
        // this.displayTerminal(10, "⇁ Core dump analyzer started");
        // this.displayTerminal(10, "⇁ Trigger executable block at #00.22");
        // this.displayTerminal(3, "⇁ authorize [[;#3c763d;]ok]");
        // this.displayTerminal(3, "⇁ c_override [[;#3c763d;]authorized]");
        this.displayTerminal(3, "⇁ remote_core_dump_analyze [[;#3c763d;]trusted]");
        this.displayTerminal(5, " ");

        this.schedule.dispatch(0, {type: WORD_SEARCH_BEGIN});

        // thread.display(0, "#wordsearch_main");


        this.displayTerminal(15, "[[i;#31708f;]↼ Remote core dump analysis session started.]");
        this.displayTerminal(5, " ");


        this.displayNextWord()

    }

    displayNextWord() {
        const rootState = this.store.getState() as WordSearchRootState
        const nextWord = rootState.puzzle.words[rootState.state.wordIndex]
        const index = `${rootState.state.wordIndex + 1}/${rootState.puzzle.words.length}`

        this.displayTerminal(0, `Search fragment: ${nextWord} ${index}`)
    }

    update(data: ServerEnterIceWordSearch) {
        if (data.hacked) {
            notify({type: "ok", title: "Result", message: "Ice hacked"});
            this.displayTerminal(0, "");
            this.displayTerminal(20, "Memory analysis complete. Status: [[b;#31708f;]success]");
            this.displayTerminal(40, "[[i;#a9443b;]↼ Ice restored, access granted.]");
            this.schedule.run(0, () => {
                window.close()
            });

        }
        this.displayNextWord()
    }
}

export const wordSearchManager = new WordSearchManager();
