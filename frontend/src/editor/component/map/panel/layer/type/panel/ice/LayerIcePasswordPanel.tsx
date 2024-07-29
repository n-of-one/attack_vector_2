import React from 'react'
import {useDispatch} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerIcePassword} from "../../../../../../../../common/model/layer/LayerIcePassword"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {AttributeIceUrlWithQr} from "../../../element/AttributeIceUrlWithQr";


interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerIcePasswordPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const ice = new LayerIcePassword(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="ICE Password" layerObject={ice}>
            <TextAttribute key={key("pa")} size="large" label="Password" value={ice.password} save={value => ice.savePassword(value)}
                           placeholder="* Password / passphrase" help="The password or passphrase the hacker needs to enter to bypass this ice."/>
            <TextAttribute key={key("hi")} size="large" label="Hint" value={ice.hint} save={value => ice.saveHint(value)}
                           placeholder="Optional hint" help="This hint is shown when the password is entered incorrectly.
                              Can be used to help hackers."/>
            <AttributeIceUrlWithQr layerId={layer.id}/>
        </LayerPanel>
    )
}
