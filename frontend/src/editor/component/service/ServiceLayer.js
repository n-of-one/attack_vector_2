import React from 'react';
import {connect} from "react-redux";
import SilentLink from "../../../common/component/SilentLink";
import {SWAP_SERVICE_LAYER} from "../../EditorActions";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {


    const swap = (node, service, layer) => {
        dispatch({type: SWAP_SERVICE_LAYER, nodeId: node.id, fromId: service.id, toId: node.services[layer].id});
    };

    return {
        up: (node, service) => swap(node, service, service.layer-1),
        down: (node, service) => swap(node, service, service.layer+1),
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, service, up, down}) => {

        const layer = service.layer;

        let downClickable = (layer > 1);
        let downHtml = downClickable ? (
            <SilentLink className="textLink" onClick={() => up(node, service)}>◀</SilentLink>) : (<span>◀</span>
        );

        let upClickable = (layer !== 0 && layer < (node.services.length-1));
        let upHtml = upClickable ? (
            <SilentLink className="textLink" onClick={() => down(node, service)}>▶</SilentLink>) : (<span>▶</span>
        );

        return (
            <div className="row form-group serviceFieldRow">
                <div className="col-lg-3 serviceLabel">Layer</div>
                <div className="col-lg-8">
                    <div className="text-muted strong service_text_label">
                        {layer}&nbsp;{downHtml}&nbsp;{upHtml}
                     </div>
                </div>
            </div>
        );
    });
