import React from 'react';
import {connect} from "react-redux";
import ServiceText from "../../../../../common/model/service/ServiceText";
import ServiceField from "../../ServiceField";
import ServicePanel from "./ServicePanel";

const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, service, dispatch}) => {

        const text = new ServiceText(service, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => service.id + ":" + param;

        return (
            <ServicePanel type="Text" serviceObject={text}>
                <ServiceField key={key("ha")} size="large" name="Hacked text" value={text.text} save={value => text.saveText(value)}
                              placeholder="* Data found: ..." help="This is the text displayed when a player hacks this service.
                              It can be used to provide data, or to simulate that some effect has taken place."/>
            </ServicePanel>
        );
    });
