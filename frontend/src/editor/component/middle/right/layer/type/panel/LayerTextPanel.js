import React from 'react';
import {connect} from "react-redux";
import LayerText from "../../../../../../../common/model/layer/LayerText";
import LayerField from "../../LayerField";
import LayerPanel from "./LayerPanel";

const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layer, dispatch}) => {

        const text = new LayerText(layer, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => layer.id + ":" + param;

        return (
            <LayerPanel type="Text" layerObject={text}>
                <LayerField key={key("ha")} size="large" name="Hacked text" value={text.text} save={value => text.saveText(value)}
                              placeholder="* Data found: ..." help="This is the text displayed when a player hacks this layer.
                              It can be used to provide data, or to simulate that some effect has taken place."/>
            </LayerPanel>
        );
    });
