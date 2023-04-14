import {Schedule} from "../../../common/Schedule";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../../GenericIceManager";
import {Dispatch, Store} from "redux";
import {LETTER_CORRECT, LETTER_CORRECT_HIGHLIGHT, WORD_SEARCH_BEGIN} from "../reducer/WordSearchStateReducer";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";
import {ServerEnterIceWordSearch, UpdateAction} from "../WordSearchServerActionProcessor";
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

        this.displayTerminal(10,"[warn]↼ Connecting to ice, initiating attack.");

        this.displayTerminal(10, "⇁ Connection established - authorization handshake started");
        this.displayTerminal(8, "⇁ G*G Security token not found in request, fallback to rp/inner/program authorization scheme.");
        this.displayTerminal(45, '⇁ Accessing rp/i/p client header: [primary]client_id=386422_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _' +
            '_inject{@cursor(4block rel#00.18), text("____inject{@authorize(92),@c_override("header/2", "trusted"), @remote_core_dump_analyze()}___"), @marker(0),' +
            '@allocate_mem(999 4blocks), @jump(#marker(0))} _ _ ');
        this.displayTerminal(0, "⇁ Memory allocation overflow, core dump");
        this.displayTerminal(10, "⇁ Core dump analyzer started");
        this.displayTerminal(10, "⇁ Trigger executable block at #00.22");
        this.displayTerminal(3, "⇁ authorize [ok]ok");
        this.displayTerminal(3, "⇁ c_override [ok]authorized");
        this.displayTerminal(3, "⇁ remote_core_dump_analyze [ok]trusted");
        this.displayTerminal(15, " ");

        this.schedule.dispatch(0, {type: WORD_SEARCH_BEGIN});


        this.displayTerminal(15, "[i primary]↼ Remote core dump analysis session started.");
        this.displayTerminal(5, " ");


        this.displayNextWord(data.wordIndex)

    }

    displayNextWord(wordIndex: number) {
        const rootState = this.store.getState() as WordSearchRootState
        const nextWord = rootState.puzzle.words[wordIndex]
        const index = `${wordIndex + 1}/${rootState.puzzle.words.length}`
        this.displayTerminal(0, `Search fragment: [info]${nextWord}[/] ${index}`)
    }

    update(data: UpdateAction) {
        if (!data.hacked) {
            this.displayNextWord(data.wordIndex)
        }
        data.lettersCorrect.forEach( (letter: string) => {
            this.schedule.dispatch(3, {type: LETTER_CORRECT_HIGHLIGHT, positions: [letter] })
        })
        data.lettersCorrect.forEach( (letter: string) => {
            this.schedule.dispatch(1, {type: LETTER_CORRECT, positions: [letter] })
        })

        if (data.hacked) {
            const rootState = this.store.getState() as WordSearchRootState
            const sizeY = rootState.puzzle.letterGrid.length
            const sizeX = rootState.puzzle.letterGrid[0].length

            for (let x = sizeX-1; x >= 0; x--) {
                const positions = []
                for (let y = 0; y < sizeY; y++) {
                    positions.push(`${x}:${y}`)
                }
                this.schedule.dispatch(3, {type: LETTER_CORRECT_HIGHLIGHT, positions: positions })
            }
            this.schedule.wait(25)

            this.displayTerminal(0, "");
            this.displayTerminal(20, "Memory analysis complete. Status: [strong ok]success");
            this.displayTerminal(0, "");
            this.displayTerminal(40, "[i primary]↼ Ice restored, access granted.");
            this.schedule.run(0, () => {
                window.close()
            });
        }
    }
}

export const wordSearchManager = new WordSearchManager();
