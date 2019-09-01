import React from 'react';
import {connect} from "react-redux";
import LayerField from "../../LayerField";
import LayerPanel from "./LayerPanel";
import LayerOs from "../../../../../../../common/model/layer/LayerOs";

const mapDispatchToProps = (dispatch) => {
    return {dispatch: dispatch}
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layer, dispatch}) => {

    const os = new LayerOs(layer, node, dispatch);

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = param => layer.id + ":" + param;

    return (
        <LayerPanel type="OS" layerObject={os}>
            <LayerField key={key("nw")} size="small" name="Network ▣" value={os.networkId} save={value => os.saveNetworkId(value)}
                          help="This is the number shown next to the node icon.
                          It is used by the hacker to navigate through the site."  placeholder="<xx>" />
            <LayerField key={key("nn")} size="large" name="Node name" value={os.nodeName} save={value => os.saveNodeName(value)}
                          placeholder="Optional name" help="When a node has a name, this is shown in the scan and when a hacker enters the node.
                          It can be used to give a node extra meaning."/>
        </LayerPanel>
    );
});
