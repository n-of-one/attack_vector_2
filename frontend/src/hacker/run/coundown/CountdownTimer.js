import React from 'react';
import {connect} from "react-redux";
import serverTime from "../../../common/ServerTime";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {
        showCountdown: state.run.countdown.finishAt !==null,
        secondsLeft: state.run.countdown.secondsLeft
    };
};


export default connect(mapStateToProps, mapDispatchToProps)(
    ({showCountdown, secondsLeft}) => {

        if (showCountdown) {
            return (
                <span className="countdown">{serverTime.format(secondsLeft)}</span>
            );
        } else {
            return <></>;
        }
    });
