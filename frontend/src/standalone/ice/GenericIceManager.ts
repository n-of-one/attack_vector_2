import {ICE_DISPLAY_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";
import {Schedule} from "../../common/util/Schedule";
import {TERMINAL_RECEIVE} from "../../common/terminal/TerminalReducer";
import {Store} from "redux";
import {avEncodedUrl} from "../../common/util/Util";

export class GenericIceManager{

    schedule: Schedule = null as unknown as Schedule

    store: Store = null as unknown as Store
    nextUrl: string | null = null

    displayTerminal(wait: number, message: string) {
        this.schedule.dispatch(wait, {type: TERMINAL_RECEIVE, terminalId: ICE_DISPLAY_TERMINAL_ID, data: message})
    }

    enterHacked() {
        this.displayTerminal(20, "↼ Connecting to ice, initiating attack.")
        this.displayTerminal(10, "↼ ICE is already hacked.")
        this.displayTerminal(0, "")

        if (this.nextUrl) {
            this.displayTerminal(60, "↼ Transitioning to next layer.")
            this.schedule.run(0, () => {
                document.location.href = avEncodedUrl(this.nextUrl!!)
            })
        }
    }

    processHacked() {
        this.schedule.run(0, () => {
            if (this.nextUrl) {
                document.location.href = avEncodedUrl(this.nextUrl)
            } else {
                window.close()
            }
        })
    }
}