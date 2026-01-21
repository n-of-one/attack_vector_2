import React from 'react'
import {useDispatch} from "react-redux"
import {LayerText} from "../../../../../../../../common/model/layer/LayerText"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {TextSaveType} from "../../../../../../../../common/component/TextSaveInput";
import {AttributeTerminalPreview} from "../../../element/AttributeTerminalPreview";
import {TextEditorLink} from "../../../element/TextEditorLink";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerTextPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const text = new LayerText(layer, node, dispatch)

    const hackedText = `Hacked: [pri]${layer.level}[/] ${layer.name}\n\n`


    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="Text" layerObject={text}>
            <TextAttribute key={key("text")} size="large" label="Hacked text" value={text.text}
                           save={value => text.saveText(value)}
                           placeholder="* Data found: ..." help="This is the text displayed when a player hacks this layer.
                              It can be used to provide data, or to simulate that some effect has taken place.
                              Links to web pages or images can be added like this: [http://example.com]click here[/]"
                           type={TextSaveType.TEXTAREA} sendEvent={true}
            />
            <TextEditorLink node={node} layer={layer} keyText="TEXT" size="large" label=""/>
            <AttributeTerminalPreview prefix={hackedText} width="594px"/>
        </LayerPanel>
    )
}


