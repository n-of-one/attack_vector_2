import React from 'react';
import {useDispatch} from "react-redux";
import LayerField from "../../LayerField";
import {LayerPanel} from "./LayerPanel";
import LayerTimerTrigger from "../../../../../../../common/model/layer/LayerTimerTrigger";
import {EditorLayerDetails, Node} from "../../../../../../reducer/NodesReducer";

interface Props {
    node: Node,
    layer: EditorLayerDetails
}

export const LayerTimerTriggerPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const layerObject = new LayerTimerTrigger(layer, node, dispatch);

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param;

    return (
        <LayerPanel typeDisplay="Timer Trigger" layerObject={layerObject}>
            <LayerField key={key("minutes")} size="large" name="Minutes" value={layerObject.minutes} save={value => layerObject.saveMinutes(value)}
                        placeholder="(Minutes until alarm)" help="Minutes part of time until alarm."/>
            <LayerField key={key("seconds")} size="large" name="Seconds" value={layerObject.seconds} save={value => layerObject.saveSeconds(value)}
                        placeholder="(Seconds part of time until alarm)" help="Seconds part of time until alarm."/>
        </LayerPanel>
    );
}
