import React, {Component} from 'react'
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/user/RequiresRole"
import {configureStore} from "@reduxjs/toolkit"
import {NAVIGATE_PAGE, Page} from "../common/menu/pageReducer"
import {Reducer, Store} from "redux"
import {initGenericServerActions} from "../common/server/GenericServerActionProcessor";
import {adminRootReducer, AdminRootState} from "./AdminRootReducer";
import {AdminPageChooser} from "./AdminPageChooser";
import {webSocketConnection, WS_UNRESTRICTED} from "../common/server/WebSocketConnection";
import {ROLE_ADMIN} from "../common/user/UserAuthorizations";


interface Props {
}

export class AdminRoot extends Component<Props> {
    adminStore: Store

    constructor(props: Props) {
        super(props)

        initGenericServerActions()

        this.adminStore = configureStore({
            reducer: adminRootReducer as Reducer<AdminRootState>
        })

        webSocketConnection.create(WS_UNRESTRICTED, this.adminStore, () => {
            webSocketConnection.sendWhenReady("/admin/logon", null)
        })

        // set up initial state:
        this.adminStore.dispatch({type: NAVIGATE_PAGE, to: Page.CONFIG})

        document.body.style.backgroundColor = "#222222"
    }

    render() {
        return (
            <RequiresRole requires={ROLE_ADMIN}>
                <Provider store={this.adminStore}>
                    <AdminPageChooser/>
                </Provider>
            </RequiresRole>
        )
    }
}
