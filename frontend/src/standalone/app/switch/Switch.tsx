import React from "react";
import {useSelector} from "react-redux";
import {SwitchRootState, SwitchState} from "./SwitchReducers";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {app} from "../../StandaloneGlobals";


export const Switch = () => {

    const state: SwitchState = useSelector((state: SwitchRootState) => state.switch)

    if (state.status === null) {
        return <div>Loading...</div>
    }

    const setValue = (newValue: boolean) => {
        webSocketConnection.sendObject("/av/app/statusLight/setValue", { appId: app.id, value: newValue})
    }

    return (
        <>
            <div className="container-fluid" data-bs-theme="dark">
                <div className="row">
                    <div className="col-12">
                        <h2>Light switch 3293</h2>
                    </div>
                </div>
                <div className="d-flex flex-row mb-3">
                    <div className="p-2 text switchLabel">{state.textForRed}</div>
                    <div className="p-2"><span className="form-check form-switch">
                                <input className="form-check-input switch" type="checkbox" role="switch" checked={state.status}
                                       onChange={() => setValue(!state.status)}
                                /></span>
                    </div>
                    <div className="p-2 text switchLabel">{state.textForGreen}</div>
                </div>
            </div>
        </>
    )
}