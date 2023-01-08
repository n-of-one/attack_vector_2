import React from 'react';
import {connect} from "react-redux";
import {SWAP_LAYERS} from "../../../../EditorActions";
import SilentLink from "../../../../../common/component/SilentLink";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {


    const swap = (node, layer, level) => {
        dispatch({type: SWAP_LAYERS, nodeId: node.id, fromId: layer.id, toId: node.layers[level].id});
    };

    return {
        up: (node, layer) => swap(node, layer, layer.level-1),
        down: (node, layer) => swap(node, layer, layer.level+1),
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layer, up, down}) => {

        const level = layer.level;

        let downClickable = (level > 1);
        let downHtml = downClickable ? (
            <SilentLink classNameInput="textLink" onClick={() => up(node, layer)}>◀</SilentLink>) : (<span>◀</span>
        );

        let upClickable = (level !== 0 && level < (node.layers.length-1));
        let upHtml = upClickable ? (
            <SilentLink classNameInput="textLink" onClick={() => down(node, layer)}>▶</SilentLink>) : (<span>▶</span>
        );

        return (
            <div className="row form-group layerFieldRow">
                <div className="col-lg-3 layerLabel">Layer</div>
                <div className="col-lg-8">
                    <div className="text-muted strong layer_text_label">
                        {level}&nbsp;{downHtml}&nbsp;{upHtml}
                     </div>
                </div>
            </div>
        );
    });
