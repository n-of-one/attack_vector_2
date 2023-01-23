import React from 'react'
import {useSelector} from "react-redux"
import {
    CODE, CORE, ICE_ALTERNATE, ICE_FILM, ICE_MAGIC_EYE, ICE_NETWALK, ICE_PASSWORD, ICE_PASSWORD_SEARCH, ICE_TANGLE,
    ICE_UNHACKABLE, ICE_WORD_SEARCH, LINK, MONEY, PICTURE, SCAN_BLOCK, TEXT, TIMER_TRIGGER, TRACE_LOG, TRACER
} from "../../../../common/enums/LayerTypes"
import Glyphicon from "../../../../common/component/Glyphicon"
import {EditorState} from "../../../EditorRootReducer"
import {sendAddLayer} from "../../../server/EditorServerClient"

export const LayersPanel = () => {

    const currentNodeId = useSelector((state: EditorState) => state.currentNodeId)

    const add = (type: string, implemented?: boolean) => {
        if (implemented && currentNodeId != null) {
            sendAddLayer({layerType: type, nodeId: currentNodeId})
        }
    }

    const regular = (type: string) => {
        return (
            <span className="btn btn-info btn-spaced" onClick={() => {
                add(type, true)
            }}>
                <Glyphicon type={type} size="18px" color="white"/>
            </span>
        )
    }

    const ice = (type: string) => {
        return (
            <span className="btn btn-info btn-spaced btn-narrowed" onClick={() => {
                add(type, true)
            }}>
                    <Glyphicon type={type} size="18px" color="NavajoWhite"/>
            </span>
        )
    }

    const unImplemented = (type: string) => {
        return (
            <span className="btn btn-grey btn-spaced">
                <Glyphicon type={type} size="18px"/>
            </span>
        )
    }

    return (
        <div className="row">
            <div className="col-lg-12 darkWell">
                <br/>
                <div>
                    {regular(TEXT)}
                    {regular(TIMER_TRIGGER)}
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
                    {ice(ICE_PASSWORD)}
                    {ice(ICE_TANGLE)}
                    {unImplemented(ICE_FILM)}
                    {unImplemented(ICE_NETWALK)}
                    {unImplemented(ICE_WORD_SEARCH)}
                    {unImplemented(ICE_MAGIC_EYE)}
                    {unImplemented(ICE_PASSWORD_SEARCH)}
                    {unImplemented(ICE_ALTERNATE)}
                    {unImplemented(ICE_UNHACKABLE)}
                </div>
                <br/>
            </div>
        </div>
    )
}
