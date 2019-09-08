import React from 'react';
import {connect} from "react-redux";
import PasswordIceHome from "./password/PasswordIceHome";
import {ICE_PASSWORD, ICE_TANGLE} from "../../../common/enums/LayerTypes";
import TangleIceHome from "./tangle/TangleIceHome";

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
            case ICE_TANGLE:
                return <TangleIceHome/>;
            default:
                return <h1 className="text">IceGame.js: Unsupported ICE: {currentIce.type}</h1>;
        }

    });
