import React, {Fragment} from 'react';
import {connect} from "react-redux";
import SilentLink from "../../common/component/SilentLink";
import editorCanvas from "./EditorCanvas";

const mapDispatchToProps = (dispatch) => {
    return {}
};
const mapStateToProps = (state) => {
    return {
        siteState: state.state,
    };
};

const openService = (nodeId, serviceId) => {
    editorCanvas.openService(nodeId, serviceId);
}


const renderMessage = (message, index) => {
    const label = (message.type === "INFO") ? <span className="label label-info">Info&nbsp;</span> : <span className="label label-warning">Error</span>;
    const link = (message.serviceId) ? (<>&nbsp;<SilentLink onClick={() => {openService(message.nodeId, message.serviceId)}}>
        <span className="glyphicon glyphicon-share-alt" /></SilentLink></>) : <></>;

    return (
        <div className="row" key={index}>
            <div className="col-lg-12 site-state-text">
                {label}{link}&nbsp;{message.text}
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
