import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {Schedule} from "../../../common/util/Schedule";
import {TERMINAL_RECEIVE} from "../../../common/terminal/TerminalReducer";
import {Dispatch, Store} from "redux";
import {avEncodedUrl} from "../../../common/util/Util";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {layer} from "../../StandaloneGlobals";

const SERVER_REDIRECT_NEXT_LAYER = "SERVER_REDIRECT_NEXT_LAYER"

export class GenericIceManager {

    schedule: Schedule = null as unknown as Schedule
    dispatch: Dispatch = null as unknown as Dispatch

    store: Store = null as unknown as Store
    externalHack: boolean = false

    init(store: Store, externalHack: boolean) {
        this.store = store;
        this.externalHack = externalHack
        this.dispatch = store.dispatch;
        this.schedule = new Schedule(store.dispatch);

        webSocketConnection.addAction(SERVER_REDIRECT_NEXT_LAYER, (data: {path?: string}) => {
            this.proceedNextLayer(data.path)
        })
    }

    displayTerminal(wait: number, message: string) {
        this.schedule.dispatch(wait, {type: TERMINAL_RECEIVE, terminalId: ICE_DISPLAY_TERMINAL_ID, data: message})
    }

    enterHacked() {
        this.displayTerminal(20, "↼ Connecting to ice, initiating attack.")
        this.displayTerminal(10, "↼ ICE is already hacked.")
        this.displayTerminal(0, "")

        this.requestNextLayer()
    }

    processHacked() {
        this.schedule.run(0, () => {
            if (this.externalHack) {
                this.requestNextLayer()
            } else {
                window.close()
            }
        })
    }

    requestNextLayer() {
        if (this.externalHack) {
            webSocketConnection.send("/ice/next", JSON.stringify({layerId: layer.id}))
        }
    }

    proceedNextLayer(path?: string) {
        if (!path) {
            this.displayTerminal(60, "↼ No underlying layers can be accessed. (You can close this tab)")
            return
        }

        this.displayTerminal(60, "↼ Transitioning to next layer.")
        this.schedule.run(0, () => {
            document.location.href = avEncodedUrl(path)
        })
    }

}
