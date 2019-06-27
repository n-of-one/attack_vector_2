import React from 'react';
import {connect} from "react-redux";
import ServiceField from "../../ServiceField";
import ServicePanel from "./ServicePanel";
import ServiceIcePassword from "../../../../../../../common/model/service/ServiceIcePassword";

const mapDispatchToProps = (dispatch) => {
    return { dispatch: dispatch }
};

let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, service, dispatch}) => {

        const ice = new ServiceIcePassword(service, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => service.id + ":" + param;

        return (
            <ServicePanel type="ICE Password" serviceObject={ice}>
                <ServiceField key={key("pa")} size="large" name="Password" value={ice.password} save={value => ice.savePassword(value)}
                              placeholder="* Password / passphrase" help="The password or passphrase the hacker needs to enter to bypass this ice."/>
                <ServiceField key={key("hi")} size="large" name="Hint" value={ice.hint} save={value => ice.saveHint(value)}
                              placeholder="Optional hint" help="This hint is shown when the password is entered incorrectly.
                              Can be used to help hackers."/>
            </ServicePanel>
        );
    });
