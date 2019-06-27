import React from 'react';
import {connect} from "react-redux";
import SilentLink from "../../../../../common/component/SilentLink";
import Glyphicon from "../../../../../common/component/Glyphicon";
import {OS} from "./ServiceTypes";
import {REMOVE_SERVICE} from "../../../../EditorActions";

const mapDispatchToProps = (dispatch) => {
    return {
        remove: (nodeId, serviceId) => dispatch({type: REMOVE_SERVICE, nodeId: nodeId, serviceId: serviceId})
    }
};
let mapStateToProps = (state) => {
    return {};
};

const renderRemove = (node, service, remove) => {
    if (service.type === OS) {
        return null;
    }
    return (
        <span className="pull-right" style={{display: "block"}}>
            <SilentLink onClick={() => remove(node.id, service.id)}>
                <Glyphicon name="glyphicon-remove" size="18px" display="block"/>
            </SilentLink>
        </span>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({type, node, service, remove}) => {

        return (
            <div className="row form-group serviceFieldTopRow">
                <div className="col-lg-3 control-label serviceLabel">Service</div>
                <div className="col-lg-8">
                    <div className="text-muted strong service_text_label text_gold">{type}
                        {renderRemove(node, service, remove)}
                    </div>
                </div>
            </div>
        );
    });
