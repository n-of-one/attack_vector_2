import React from 'react';
import {connect} from "react-redux";
import RunHome from "./RunHome";
import IceGame from "../ice/IceGame";

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

        if (currentIce.type) {
            return <IceGame />
        } else {
            return <RunHome/>;
        }
    });
