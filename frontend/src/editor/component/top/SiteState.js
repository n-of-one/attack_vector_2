import React from 'react';
import {connect} from "react-redux";
import SilentLink from "../../../common/component/SilentLink";
import editorCanvas from "../middle/middle/EditorCanvas";
import {SELECT_LAYER} from "../../EditorActions";

const mapDispatchToProps = (dispatch) => {
    return {
        navigateToLayer: (nodeId, layerId) => {
            editorCanvas.openLayer(nodeId, layerId);
            dispatch({type: SELECT_LAYER, layerId: layerId })
        }
    }
};

const mapStateToProps = (state) => {
    return {
        siteState: state.state,
    };
};

const renderMessage = (message, index, navigateToLayer) => {
    const label = (message.type === "INFO") ? <span className="label label-info">Info&nbsp;</span> : <span className="label label-warning">Error</span>;
    const link = (message.layerId) ? (<>&nbsp;<SilentLink onClick={() => {navigateToLayer(message.nodeId, message.layerId)}}>
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
    ({siteState, navigateToLayer}) => {

        let statusElement = (siteState.ok) ? <span className="badge badge-success" style={{fontSize: "100%"}}>Ok</span> :
            <span className="badge badge-warning" style={{fontSize: "100%"}}>Error</span>;

        return (
            <div className="site-state">
                <div className="row">
                    <div className="col-lg-12 site-state-text">
                        Status:&nbsp;{statusElement}
                    </div>
                </div>
                <hr className="thin-hr"/>
                {siteState.messages.map( (message, index) => {
                    return renderMessage(message, index, navigateToLayer)
                })}
            </div>
        );
    });
