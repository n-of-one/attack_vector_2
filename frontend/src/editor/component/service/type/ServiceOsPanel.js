import React from 'react';
import {connect} from "react-redux";
import ServiceField from "../ServiceField";
import ServiceOs from "./ServiceOs";
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
        <ServicePanel name="OS">
            <ServiceField key={key("nw")} size="small" name="Network ▣" value={os.networkId} placeholder="00" save={value => os.saveNetworkId(value)}/>
            <ServiceField key={key("nn")} size="large" name="Node name" value={os.name} placeholder="Optional name" save={value => os.saveName(value)}/>
            <ServiceField key={key("no")} size="large" name="Gm Note" value={os.note} placeholder="Players will never see this." save={value => os.saveNote(value)}/>
        </ServicePanel>
    );
});
