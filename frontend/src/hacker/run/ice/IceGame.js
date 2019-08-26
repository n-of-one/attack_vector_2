import React from 'react';
import {connect} from "react-redux";
import PasswordIceHome from "./password/PasswordIceHome";
import {ICE_PASSWORD} from "../../../common/enums/ServiceTypes";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {
        currentIce: state.run.ice.currentIce
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentIce}) => {
        switch (currentIce.type) {
            case null:
                return <></>;
            case ICE_PASSWORD:
                return <PasswordIceHome/>;
            default:
                return <h1 className="text">Unsupported ICE: {currentIce}</h1>;
        }

    });
