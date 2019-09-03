import React from 'react';
import {connect} from "react-redux";
import LayerPanel from "./LayerPanel";
import LayerStrength from "../../LayerStrength";
import LayerIceTangle from "../../../../../../../common/model/layer/LayerIceTangle";

const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layer, dispatch}) => {

        const ice = new LayerIceTangle(layer, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => layer.id + ":" + param;

        return (
            <LayerPanel type="ICE (Un)tangle" layerObject={ice}>
                <LayerStrength key={key("strength")} value={ice.strength} save={value => ice.saveStrength(value)} />
            </LayerPanel>
        );
    });
