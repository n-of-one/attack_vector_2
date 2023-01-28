import React from 'react';
import {connect} from "react-redux";
import RunHome from "./RunHome";
import {IceGame} from "../ice/IceGame";
import AlarmTimer from "../coundown/CountdownTimer";

const mapDispatchToProps = (dispatch) => {
    return {}
};

let mapStateToProps = (state) => {
    return {
        currentIce: state.run.ice.currentIce
    };
};

const ice = (currentIce) => {
    if (currentIce.type) {
        return <IceGame/>
    } else {
        return <></>
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentIce}) => {

        const runHomeStyle = (currentIce.type) ? "none" : "inline";

        return <>
            <AlarmTimer />
            {ice(currentIce)}
            <span style={{"display": runHomeStyle}}>
                    <RunHome/>
                </span>
        </>
    });
