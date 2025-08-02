import React from 'react'
import {useDispatch} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {LayerScriptCredits} from '../../../../../../../../common/model/layer/LayerScriptCredits';
import {AttributeDropdown, DropdownOption} from "../../../element/AttributeDropdown";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerScriptCreditsPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const scriptCredits = new LayerScriptCredits(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    const options: DropdownOption[] = [{value: "false", text: "Credits can be stolen."},
        {value: "true", text: "Credits have been stolen."}]

    return (
        <LayerPanel typeDisplay="Script Credits" layerObject={scriptCredits}>
            <TextAttribute key={key("value")} size="small" label="Value" value={scriptCredits.amount} save={value => scriptCredits.saveAmount(value)}
                           help="The amount of credits the data is worth. This is the amount of credits the hacker will receive when they hack this layer."
                           placeholder="10"/>
            <AttributeDropdown options={options} tooltipId="status" key={key("display")} label="Status" value={scriptCredits.stolen.toString()}
                               save={value => scriptCredits.saveStolen(value)}
                               tooltipText="The data that is worth the credits can only be stolen once. After a hacker hacks this node, the status becomes
                               'Credits have been stolen'. Note that this will not reset when the site is reset. If you want to allow hackers to steal these
                               credits again, you need to manually set this value to 'Credits can be stolen'."
            />
        </LayerPanel>
    )
}
