import React from 'react'
import {useSelector} from "react-redux"
import {
    CODE, CORE, ALTERNATE_ICE, ICE_FILM, MAGIC_EYE_ICE, NETWALK_ICE, PASSWORD_ICE, TANGLE_ICE,
    UNHACKABLE_ICE, WORD_SEARCH_ICE, LINK, MONEY, PICTURE, SCAN_BLOCK, TEXT, TIMER_TRIGGER, TRACE_LOG, TRACER
} from "../../../../common/enums/LayerTypes"
import {Glyphicon} from "../../../../common/component/Glyphicon"
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
                    {ice(PASSWORD_ICE)}
                    {ice(TANGLE_ICE)}
                    {ice(NETWALK_ICE)}
                    {ice(WORD_SEARCH_ICE)}
                    {unImplemented(ICE_FILM)}
                    {unImplemented(MAGIC_EYE_ICE)}
                    {unImplemented(ALTERNATE_ICE)}
                    {unImplemented(UNHACKABLE_ICE)}
                </div>
                <br/>
            </div>
        </div>
    )
}
