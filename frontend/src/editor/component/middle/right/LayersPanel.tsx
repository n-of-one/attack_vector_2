import React from 'react'
import {useSelector} from "react-redux"
import {
    CODE, CORE, SLOW_ICE, NETWALK_ICE, PASSWORD_ICE, TANGLE_ICE,
    WORD_SEARCH_ICE, LINK, MONEY, PICTURE, SCAN_BLOCK, TEXT, TIMER_TRIGGER, TRACE_LOG, TRACER, LOCK, STATUS_LIGHT
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
            webSocketConnection.send("/av/editor/addLayer", payload)
        }
    }

    const regular = (type: string, color: string) => {
        return (
            <span className="btn btn-info btn-spaced" onClick={() => {
                add(type, true)
            }}>
                <Icon type={type} size="18px" color={color}/>
            </span>
        )
    }



    const unImplemented = (type: string) => {
        return (
            <span className="btn btn-grey btn-spaced">
                <Icon type={type} size="18px"/>
            </span>
        )
    }


    //
    // const statusLight = (type: string) => {
    //     const boxPadding = {paddingLeft: "9px", paddingTop: "5px", paddingRight: "9px", paddingBottom: "5px"}
    //     if (type === STATUS_LIGHT_DOOR) {
    //         return (
    //             <span className="btn btn-info btn-spaced" style={boxPadding} onClick={ () => { addStatusLight("DOOR") } }>
    //             <BoxIcon type="door" color="royalblue"/>
    //             </span>
    //         )
    //     }
    //     if (type === STATUS_LIGHT_ON_OFF) {
    //         return (
    //             <span className="btn btn-info btn-spaced" onClick={ () => { addStatusLight("ON_OFF") } }>
    //                 <Glyphicon name="glyphicon-adjust" size="18px" color="royalblue"/>
    //             </span>
    //         )
    //     }
    // }

    return (
        <div className="row">
            <div className="col-lg-12 darkWell">
                <br/>
                <div>
                    {regular(TEXT, "white")}
                    {regular(TIMER_TRIGGER, "white")}
                    {unImplemented(PICTURE)}
                    {unImplemented(LINK)}
                    {unImplemented(TRACER)}
                    {unImplemented(TRACE_LOG)}
                    {unImplemented(SCAN_BLOCK)}
                    {unImplemented(MONEY)}
                    {unImplemented(CODE)}
                    {unImplemented(CORE)}
                </div>
                <div className="btn-height-spacer"/>
                <div>
                    {regular(PASSWORD_ICE, "NavajoWhite")}
                    {regular(TANGLE_ICE, "NavajoWhite")}
                    {regular(NETWALK_ICE, "NavajoWhite")}
                    {regular(WORD_SEARCH_ICE, "NavajoWhite")}
                    {regular(SLOW_ICE, "NavajoWhite")}
                    {regular(LOCK, "royalblue")}
                    {regular(STATUS_LIGHT, "royalblue")}
                </div>
                <br/>
            </div>
        </div>
    )
}
