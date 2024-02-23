import React, {ReactNode} from 'react'
import {useSelector} from "react-redux"
import {findElementById} from "../../../../../../../common/util/Immutable"
import {LayerType} from "../../element/LayerType"
import {LayerLevel} from "../../element/LayerLevel"
import {Layer} from "../../../../../../../common/model/layer/Layer"
import {EditorState} from "../../../../../../EditorRootReducer"
import {LayerField} from "../../element/LayerField"
import {createSelector} from "@reduxjs/toolkit";

interface Props {
    layerObject: Layer,
    children: ReactNode,
    typeDisplay: string
}

const selectCurrentNodeId = (state: EditorState) => state.currentNodeId
const selectCurrentLayerId = (state: EditorState) => state.currentLayerId
const selectNodes = (state: EditorState) => state.nodes

const selectNodeAndLayerData = createSelector(selectCurrentNodeId, selectCurrentLayerId, selectNodes, (currentNodeId, currentLayerId, nodes) => {
    if (!currentNodeId) {
        return {}
    }
    const node = findElementById(nodes, currentNodeId)
    const layer = findElementById(node.layers, currentLayerId!)

    return {
        node: node,
        layerData: layer,
    }
})

export const LayerPanel = ({layerObject, children, typeDisplay}: Props) => {

    const {node, layerData} = useSelector(selectNodeAndLayerData)

    if (!node) {
        return <div/>
    }

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layerData.id + ":" + param

    return (
        <div className="tab-content" id="node-layers-tab-content ">
            <div className="tab-pane active">
                <LayerType typeDisplay={typeDisplay} node={node} layer={layerData}/>
                <LayerLevel layer={layerData} node={node}/>
                <LayerField key={key("id")} size="small" label="Layer id" value={layerObject.id} readOnly={true}
                            help="Unique ID of this layer. Used when layers refer to each other." id="layerId"/>
                <LayerField key={key("name")} size="small" label="Layer name" value={layerObject.name} save={value => layerObject.saveName(value)}
                            placeholder="As seen by hackers" help="When a hacker 'scans' or 'views' a node they will see the layers by this name.
                            For ICE unique names help with passcodes" id="layerName"/>
                {children}
                <LayerField key={key("note")} size="large" label="Note" value={layerObject.note} save={value => layerObject.saveNote(value)}
                            placeholder="" help="Hacker will never see this. Notes can help to understand the design of a site." id={`gmNote-${layerObject.id}`}/>
            </div>
        </div>
    )
}
