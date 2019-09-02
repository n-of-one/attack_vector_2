import React from 'react';
import {connect} from "react-redux";
import LayerField from "../../LayerField";
import LayerPanel from "./LayerPanel";
import LayerIcePassword from "../../../../../../../common/model/layer/LayerIcePassword";
import LayerStrength from "../../LayerStrength";

const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layer, dispatch}) => {

        const ice = new LayerIcePassword(layer, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => layer.id + ":" + param;

        return (
            <LayerPanel type="ICE (Un)tangle" layerObject={ice}>
                <LayerStrength key={key("st")} value={ice.strength} save={value => ice.savePassword(value)} />
            </LayerPanel>
        );
    });
