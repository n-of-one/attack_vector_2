import {IceStrength} from "../../../common/model/IceStrength";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {jigsawIceManager} from "./JigsawIceManager";
import {SERVER_ICE_HACKED, SERVER_RESET_ICE} from "../../../common/server/GenericServerActionProcessor";
import {PieceConfig} from "./component/JigsawShapes";

export const SERVER_JIGSAW_ENTER = "SERVER_JIGSAW_ENTER"

export interface JigsawEnterData {
    hacked: boolean,
    strength: IceStrength,
    imageSrc: string,
    columns: number,
    rows: number,
    pieces: PieceConfig[],
}

export const initJigsawServerActions = () => {
    webSocketConnection.addAction(SERVER_JIGSAW_ENTER, (data: JigsawEnterData) => {
        jigsawIceManager.enter(data)
    })
    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        // stub for step 1
    })
    webSocketConnection.addAction(SERVER_RESET_ICE, () => {
        jigsawIceManager.processIceReset()
    })
}
