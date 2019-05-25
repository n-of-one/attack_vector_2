import React from 'react';
import {connect} from "react-redux";

const mapDispatchToProps = (dispatch) => {
    return {}
};
const mapStateToProps = (state) => {
    return {
        siteState: state.state,
    };
};

const renderMessage = (message, index) => {
    const label = (message.type === "INFO") ? <span className="label label-info">Info&nbsp;</span> : <span className="label label-warning">Error</span>;

    return (
        <div className="row" key={index}>
            <div className="col-lg-12 site-state-text">
                {label}&nbsp;{message.text}
            </div>
        </div>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({siteState}) => {

        let statusElement = (siteState.ok) ? <span className="label label-success">Ok</span> :  <span className="label label-warning">Error</span>;

        return (
            <div className="site-state">
                <div className="row">
                    <div className="col-lg-12 site-state-text">
                        Status:&nbsp;{statusElement}
                    </div>
                </div>
                <hr className="thin-hr"/>
                {siteState.messages.map( (message, index) => {
                    return renderMessage(message, index)
                })}
            </div>
        );
    });
