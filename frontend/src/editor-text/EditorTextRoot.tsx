import React, {Component} from 'react'
import {Provider} from "react-redux"
import {editorTextRootReducer, EditorTextState} from "./EditorTextRootReducer"
import {Reducer, Store} from "redux"
import {webSocketConnection, WS_UNRESTRICTED} from "../common/server/WebSocketConnection"
import {configureStore} from "@reduxjs/toolkit"
import {SERVER_SITE_FULL} from "../editor/server/EditorServerActionProcessor"
import {EditLayerHome} from "./component/EditorTextHome";
import {SERVER_SCRIPT_TYPES} from "../common/script/type/ScriptTypeReducer";


const LAYER_TYPE = "LAYER"
const SCRIPT_EFFECT_TYPE = "EFFECT"


interface Props {
    path: string,
}

export class EditorTextRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        document.body.style.backgroundColor = "#333"
        document.body.style.fontSize = "14px"

        const parts = props.path.split("|")

        const initState = initStateFromParts(parts)
        const type = parts[0]
        const siteId = parts[1]

        const developmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: editorTextRootReducer as Reducer<EditorTextState>,
            preloadedState: initState,
            devTools: developmentServer
        })

        const waitFor = LAYER_TYPE === type ? SERVER_SITE_FULL : SERVER_SCRIPT_TYPES

        webSocketConnection.create(WS_UNRESTRICTED, this.store, () => {
            if (LAYER_TYPE === type) {
                webSocketConnection.subscribe('/topic/site/' + siteId)
                webSocketConnection.sendWhenReady("/editor/enter", siteId)
            } else if (SCRIPT_EFFECT_TYPE === type) {
                webSocketConnection.send("/gm/scriptType/getAll", null)
            }
        }, waitFor)

    }

    render() {
        return (
            <Provider store={this.store}>
                <EditLayerHome/>
            </Provider>
        )
    }
}

const initStateFromParts = (parts: string[]) => {
    const type = parts[0]
    if ("LAYER" === type) {
        const siteId = parts[1]
        const nodeId = parts[2]
        const layerId = parts[3]
        const key = parts[4]
        const header = parts[5]

        return {
            type: LAYER_TYPE,
            header: header,
            terminalPreview: "",
            initialValue: "",
            siteId: siteId,
            nodeId: nodeId,
            layerId: layerId,
            key: key,
            scriptTypeId: "",
            effectNumber: 0,
        }
    } else if (SCRIPT_EFFECT_TYPE === type) {

        const scriptTypeId = parts[1]
        const scriptEffectNumber = parts[2]
        const header = parts[3]

        return {
            type: SCRIPT_EFFECT_TYPE,
            header: header,
            terminalPreview: "",
            initialValue: "",
            siteId: "",
            nodeId: "",
            layerId: "",
            key: "",
            scriptTypeId: scriptTypeId,
            effectNumber: parseInt(scriptEffectNumber),
        }
    } else {
        alert("Invalid URL, type not found.")
        throw Error("Invalid URL, type not found.")
    }
}

