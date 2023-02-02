import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {Schedule} from "../../../common/Schedule";
import {TERMINAL_RECEIVE} from "../../../common/terminal/TerminalReducer";

export class GenericIceManager{

    schedule: Schedule = null as unknown as Schedule

    displayTerminal(wait: number, message: string) {
        this.schedule.dispatch(wait, {type: TERMINAL_RECEIVE, terminalId: ICE_DISPLAY_TERMINAL_ID, data: message})
    }
}