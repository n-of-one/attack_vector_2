import React from 'react'
import {useSelector} from "react-redux"
import {
    CODE,
    CORE,
    KEYSTORE,
    LOCK,
    MONEY,
    NETWALK_ICE,
    PASSWORD_ICE,
    SCAN_BLOCK,
    STATUS_LIGHT,
    TANGLE_ICE,
    TAR_ICE,
    TEXT,
    TRACE_LOG,
    TRACER,
    TRIPWIRE,
    WORD_SEARCH_ICE
} from "../../../../common/enums/LayerTypes"
import {EditorState} from "../../../EditorRootReducer"
import {editorSiteId} from "../../../EditorRoot";
import {webSocketConnection} from "../../../../common/server/WebSocketConnection";
import {Icon} from "../../../../common/component/icon/Icon";


export const LayersPanel = () => {

    const currentNodeId = useSelector((state: EditorState) => state.currentNodeId)

    const add = (type: string, implemented?: boolean) => {
        if (implemented && currentNodeId != null) {
            const payload = {siteId: editorSiteId, layerType: type, nodeId: currentNodeId}
            webSocketConnection.send("/editor/addLayer", payload)
        }
    }

    const regular = (type: string, color: string) => {
        return (
            <span className="btn btn-info btn-spaced" onClick={() => {
                add(type, true)
            }}>
                <Icon type={type} color={color}/>
            </span>
        )
    }



    const unImplemented = (type: string) => {
        return (
            <span className="btn btn-grey btn-spaced">
                <Icon type={type} />
            </span>
        )
    }

    return (
        <div className="row" style={{marginTop: "4px"}}>
            <div className="col-lg-12 darkWell">
                <br/>
                <div>
                    {regular(TEXT, "white")}
                    {regular(KEYSTORE, "white")}
                    {regular(TRIPWIRE, "white")}
                    {regular(CORE, "white")}
                    {/*{unImplemented(TIMER_TRIGGER)}*/}
                    {/*{unImplemented(TRACER)}*/}
                    {/*{unImplemented(TRACE_LOG)}*/}
                    {/*{unImplemented(SCAN_BLOCK)}*/}
                    {/*{unImplemented(MONEY)}*/}
                    {/*{unImplemented(CODE)}*/}
                </div>
                <div className="btn-height-spacer"/>
                <div>
                    {regular(PASSWORD_ICE, "NavajoWhite")}
                    {regular(TANGLE_ICE, "NavajoWhite")}
                    {regular(NETWALK_ICE, "NavajoWhite")}
                    {regular(WORD_SEARCH_ICE, "NavajoWhite")}
                    {regular(TAR_ICE, "NavajoWhite")}
                    {regular(LOCK, "royalblue")}
                    {regular(STATUS_LIGHT, "royalblue")}
                </div>
                <br/>
            </div>
        </div>
    )
}
