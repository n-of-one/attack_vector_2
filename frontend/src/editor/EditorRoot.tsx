import React, {Component} from 'react'
import {Provider} from "react-redux"
import {EditorHome} from "./component/EditorHome"
import {editorRootDefaultState, editorRootReducer, EditorState} from "./EditorRootReducer"
import {Reducer, Store} from "redux"
import {webSocketConnection, WS_UNRESTRICTED} from "../common/server/WebSocketConnection"
import {configureStore} from "@reduxjs/toolkit"
import {initEditorServerActions, SERVER_SITE_FULL} from "./server/EditorServerActionProcessor"
import {initGenericServerActions} from "../common/server/GenericServerActionProcessor";

interface Props {
    siteId: string,
}

export let editorSiteId: string = null as unknown as string

export class EditorRoot extends Component<Props> {

    errorMessage: string | null
    store: Store

    constructor(props: Props) {
        super(props)

        this.errorMessage = null
        editorSiteId = props.siteId

        if (!props.siteId.startsWith("site-")) {
            this.errorMessage = "Cannot enter site name directly in URL"
            this.state = { initSuccess: false }
            this.store = null as unknown as Store
            return
        }
        document.body.style.backgroundColor = "#f5f5f5"
        document.body.style.fontSize = "14px"

        const initState = editorRootDefaultState
        initState.siteProperties.siteId = props.siteId

        const developmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: editorRootReducer as Reducer<EditorState>,
            preloadedState: initState,
            devTools: developmentServer
        })

        initEditorServerActions()
        initGenericServerActions({fontSize: 14})

        webSocketConnection.create(WS_UNRESTRICTED, this.store, () => {
            webSocketConnection.subscribe('/topic/site/' + props.siteId)
            webSocketConnection.sendWhenReady("/editor/enter", props.siteId)
        }, SERVER_SITE_FULL)

    }

    render() {
        if (this.errorMessage) {
            return <h1 className="text">{ this.errorMessage } <a href="/">Continue.</a></h1>
        }
        return(
            <Provider store={this.store}>
                <EditorHome />
            </Provider>
        )
    }
}

