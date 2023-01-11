import React from 'react';
import {connect} from "react-redux";
import SilentLink from "../../../../../common/component/SilentLink";
import Glyphicon from "../../../../../common/component/Glyphicon";
import {OS} from "../../../../../common/enums/LayerTypes";
import {REMOVE_LAYER} from "../../../../EditorActions";

const mapDispatchToProps = (dispatch) => {
    return {
        remove: (nodeId, layerId) => dispatch({type: REMOVE_LAYER, nodeId: nodeId, layerId: layerId})
    }
};
let mapStateToProps = (state) => {
    return {};
};

const renderRemove = (node, layer, remove) => {
    if (layer.type === OS) {
        return null;
    }
    return (
        <span className="pull-right" style={{display: "block"}}>
            <SilentLink onClick={() => remove(node.id, layer.id)}>
                <Glyphicon name="glyphicon-remove" size="18px" display="block"/>
            </SilentLink>
        </span>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({type, node, layer, remove}) => {

        return (
            <div className="row form-group layerFieldTopRow">
                <div className="col-lg-3 control-label layerLabel">Type</div>
                <div className="col-lg-8">
                    <div className="strong layer_text_label text_gold d-flex justify-content-between">
                        <span>{type}</span>
                        <span>{renderRemove(node, layer, remove)}</span>
                    </div>
                </div>
            </div>
        );
    });
