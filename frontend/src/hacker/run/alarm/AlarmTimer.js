import React from 'react';
import {connect} from "react-redux";
import serverTime from "../../../common/ServerTime";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {
        secondsUntilAlarm: state.run.alarm.secondsUntilAlarm
    };
};


export default connect(mapStateToProps, mapDispatchToProps)(
    ({secondsUntilAlarm}) => {

        if (secondsUntilAlarm) {
            return (
                <span className="alarm">{serverTime.format(secondsUntilAlarm)}</span>
            );
        } else {
            return <></>;
        }
    });
