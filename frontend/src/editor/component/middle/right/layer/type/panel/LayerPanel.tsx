import React, {ReactNode} from 'react'
import {useSelector} from "react-redux"
import {findElementById} from "../../../../../../../common/Immutable"
import {LayerType} from "../../LayerType"
import {LayerLevel} from "../../LayerLevel"
import Layer from "../../../../../../../common/model/layer/Layer"
import {EditorState} from "../../../../../../EditorRootReducer"
import {LayerField} from "../../LayerField"

interface Props {
    layerObject: Layer,
    children: ReactNode,
    typeDisplay: string
}

export const LayerPanel = ({layerObject, children, typeDisplay}: Props) => {

    const {node, layerData} = useSelector((state: EditorState) => {
        if (!state.currentNodeId) {
            return {}
        }
        const node = findElementById(state.nodes, state.currentNodeId)
        const layer = findElementById(node.layers, state.currentLayerId)

        return {
            node: node,
            layerData: layer,
        }
    })

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
                <LayerField key={key("id")} size="small" name="Layer id" value={layerObject.id} readOnly={true}
                            help="Unique ID of this layer. Used when layers refer to each other."/>
                <LayerField key={key("name")} size="small" name="Layer name" value={layerObject.name} save={value => layerObject.saveName(value)}
                            placeholder="As seen by hackers" help="When a hacker 'scans' or 'views' a node they will see the layers by this name."/>
                {children}
                <LayerField key={key("note")} size="large" name="Gm Note" value={layerObject.note} save={value => layerObject.saveNote(value)}
                            placeholder="" help="Players will never see this. Notes can help to understand the design of a site."/>
            </div>
        </div>
    )
}
