import React from 'react'
import {useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerCore} from "../../../../../../../../common/model/layer/LayerCore";
import {AttributeDropdown} from "../../../element/AttributeDropdown";
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";


interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerCorePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const core = new LayerCore(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="Core" layerObject={core}>
            <AttributeDropdown key={key("status")} label="Network"
                               value={"" + core.revealNetwork}
                               options={[{value: "false", text: "Don't reveal network"},
                                    {value: "true", text: "Reveal network"}]}
                               save={value => core.saveRevealNetwork(value)}
                               tooltipId="reveal_network" tooltipText="If a hacker hacks the core, do they instantly reveal the entire network?"/>
        </LayerPanel>
    )
}
