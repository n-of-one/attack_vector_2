import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {SilentLink} from "../../../common/component/SilentLink"
import {editorCanvas} from "../map/canvas/EditorCanvas"
import {EditorState} from "../../EditorRootReducer"
import {SiteStateMessage} from "../../reducer/SiteStateReducer"
import {SELECT_LAYER} from "../../reducer/CurrentLayerIdReducer"


export const SiteState = () => {
    const siteState = useSelector((state: EditorState) => state.state)
    const siteProperties = useSelector((state: EditorState) => state.siteProperties)
    const dispatch = useDispatch()
    const navigateToLayer = (nodeId: string, layerId: string) => {
        editorCanvas.selectNode(nodeId)
        dispatch({type: SELECT_LAYER, layerId: layerId})
    }

    let statusElement = (siteProperties.siteStructureOk) ? <span className="badge bg-success" style={{fontSize: "100%"}}>Ok</span> :
        <span className="badge bg-warning" style={{fontSize: "100%"}}>Error</span>

    return (
        <div className="site-state">
            <div className="row">
                <div className="col-lg-12 site-state-text">
                    Status:&nbsp;{statusElement}
                </div>
            </div>
            <hr className="thin-hr"/>
            {siteState.messages.map((message, index) => {
                return renderMessage(message, index, navigateToLayer)
            })}
        </div>
    )
}

const renderMessage = (
    message: SiteStateMessage,
    index: number,
    navigateToLayer: (nodeId: string, layerId: string) => void) => {

    const label = (message.type === "INFO") ? <span className="badge bg-info">Info&nbsp;</span> : <span className="badge bg-warning">Error</span>
    const link = (message.layerId) ? (
            <>&nbsp;<SilentLink onClick={() => {
                navigateToLayer(message.nodeId!, message.layerId!)
            }}><span className="glyphicon glyphicon-share-alt"/></SilentLink></>) :
        <></>

    return (
        <div className="row" key={index}>
            <div className="col-lg-12 site-state-text">
                {label}{link}&nbsp;{message.text}
            </div>
        </div>
    )
}
