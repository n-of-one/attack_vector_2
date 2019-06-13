import React from 'react';
import {connect} from "react-redux";
import ServiceField from "../../ServiceField";
import ServiceOs from "../../../../../common/model/service/ServiceOs";
import ServicePanel from "./ServicePanel";

const mapDispatchToProps = (dispatch) => {
    return {dispatch: dispatch}
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, service, dispatch}) => {

    const os = new ServiceOs(service, node, dispatch);

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = param => service.id + ":" + param;

    return (
        <ServicePanel type="OS" serviceObject={os}>
            <ServiceField key={key("nw")} size="small" name="Network ▣" value={os.networkId} save={value => os.saveNetworkId(value)}
                          help="This is the number shown next to the node icon.
                          It is used by the hacker to navigate through the site."  placeholder="<xx>" />
            <ServiceField key={key("nn")} size="large" name="Node name" value={os.nodeName} save={value => os.saveNodeName(value)}
                          placeholder="Optional name" help="When a node has a name, this is shown in the scan and when a hacker enters the node.
                          It can be used to give a node extra meaning."/>
        </ServicePanel>
    );
});
