import React, {Component} from 'react'
import {Provider} from "react-redux"
import {EditorHome} from "./component/EditorHome"
import {editorRootReducer, editorRootDefaultState, EditorState} from "./EditorRootReducer"
import {Reducer, Store} from "redux"
import {RequiresRole} from "../common/RequiresRole"
import {WEBSOCKET_RUN, webSocketConnection} from "../common/WebSocketConnection"
import {configureStore} from "@reduxjs/toolkit"
import {initEditorServerActions, SERVER_SITE_FULL} from "./server/EditorServerActionProcessor"

interface Props {
    siteId: string,
}

export let editorSiteId: string = null as unknown as string

export class EditorRoot extends Component<Props> {

    errorMessage: string | null
    store: Store

    constructor(props: Props) {
        super(props)

        console.log("constructing editorRoot")
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

        webSocketConnection.create(WEBSOCKET_RUN, this.store, () => {
            webSocketConnection.subscribe('/topic/site/' + props.siteId)
            webSocketConnection.send("/av/editor/siteFull", props.siteId)
        }, SERVER_SITE_FULL)

        initEditorServerActions()
    }

    render() {
        if (this.errorMessage) {
            return <h1 className="text">{ this.errorMessage } <a href="/">Continue.</a></h1>
        }
        return(
            <RequiresRole requires="ROLE_SITE_MANAGER">
                <Provider store={this.store}>
                    <EditorHome />
                </Provider>
            </RequiresRole>
        )
    }
}

