import React from 'react';
import {useDispatch} from "react-redux";
import LayerField from "../../LayerField";
import {LayerPanel} from "./LayerPanel";
import LayerIcePassword from "../../../../../../../common/model/layer/LayerIcePassword";
import {EditorLayerDetails, Node} from "../../../../../../reducer/NodesReducer";


interface Props {
    node: Node,
    layer: EditorLayerDetails
}

export const LayerIcePasswordPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const ice = new LayerIcePassword(layer, node, dispatch);

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param;

    return (
        <LayerPanel typeDisplay="ICE Password" layerObject={ice}>
            <LayerField key={key("pa")} size="large" name="Password" value={ice.password} save={value => ice.savePassword(value)}
                        placeholder="* Password / passphrase" help="The password or passphrase the hacker needs to enter to bypass this ice."/>
            <LayerField key={key("hi")} size="large" name="Hint" value={ice.hint} save={value => ice.saveHint(value)}
                        placeholder="Optional hint" help="This hint is shown when the password is entered incorrectly.
                              Can be used to help hackers."/>
        </LayerPanel>
    );
}
