import React from 'react';
import {connect} from "react-redux";
import LayerField from "../../LayerField";
import LayerPanel from "./LayerPanel";
import LayerTimerTrigger from "../../../../../../../common/model/layer/LayerTimerTrigger";

const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layer, dispatch}) => {

        const text = new LayerTimerTrigger(layer, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => layer.id + ":" + param;

        return (
            <LayerPanel type="Timer Trigger" layerObject={text}>
                <LayerField key={key("minutes")} size="large" name="Minutes" value={text.minutes} save={value => text.saveMinutes(value)}
                              placeholder="(Minutes until alarm)" help="Minutes part of time until alarm."/>
                <LayerField key={key("seconds")} size="large" name="Seconds" value={text.seconds} save={value => text.saveSeconds(value)}
                            placeholder="(Seconds part of time until alarm)" help="Seconds part of time until alarm."/>
            </LayerPanel>
        );
    });
