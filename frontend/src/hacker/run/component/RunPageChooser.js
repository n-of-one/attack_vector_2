import React from 'react';
import {connect} from "react-redux";
import RunHome from "./RunHome";
import {ICE_PASSWORD} from "../../../common/enums/ServiceTypes";
import IceGame from "../ice/IceGame";

const mapDispatchToProps = (dispatch) => {
    return {}
};

let mapStateToProps = (state) => {
    return {
        currentIce: state.run.ice.currentIce
    };
};

const icePage = (currentIce) => {
    switch (currentIce) {
        case ICE_PASSWORD:
            return <IceGame/>;
        default:
            return <h1>Ice not yet implemented: {currentIce}</h1>;
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentIce}) => {

        if (currentIce) {
            return icePage(currentIce);
        } else {
            return <RunHome/>;
        }


    });
