import React from 'react';
import {connect} from "react-redux";
import {ADD_LAYER} from "../../../EditorActions";
import {
    CODE, CORE, ICE_ALTERNATE, ICE_FILM, ICE_MAGIC_EYE, ICE_NETWALK, ICE_PASSWORD, ICE_PASSWORD_SEARCH,
    ICE_UNHACKABLE, ICE_WORD_SEARCH, LINK, MONEY, PICTURE, SCAN_BLOCK, TEXT, TIME, TRACE_LOG, TRACER
} from "../../../../common/enums/LayerTypes";
import Glyphicon from "../../../../common/component/Glyphicon";

const mapDispatchToProps = (dispatch) => {
    return {
        add: (type, nodeId, implemented) => {
            if (implemented && nodeId != null) {
                dispatch({type: ADD_LAYER, layerType: type, nodeId: nodeId})
            }
        },
    }
};

let mapStateToProps = (state) => {
    return {state: state, currentNodeId: state.currentNodeId};
};


export default connect(mapStateToProps, mapDispatchToProps)(
    ({add, currentNodeId}) => {

        const regular = (type) => {
            return (
                <span className="btn btn-info btn-spaced" onClick={() => { add(type, currentNodeId, true) }}>
                    <Glyphicon type={type} size="18px"/>
                </span>
            );
        };

        const ice = (type) => {
            return (
                <span className="btn btn-info btn-spaced btn-narrowed" onClick={() => { add(type, currentNodeId, true) }}>
                    <Glyphicon type={type} size="18px" color="NavajoWhite"/>
                </span>
            );
        };

        const unImplemented = (type) => {
            return (
                <span className="btn btn-grey btn-spaced">
                    <Glyphicon type={type} size="18px"/>
                </span>
            );
        };


        return (
            <div className="row">
                <div className="col-lg-12 darkWell">
                    <br/>
                    <div>
                        {regular(TEXT)}
                        {unImplemented(PICTURE)}
                        {unImplemented(LINK)}
                        {unImplemented(TRACER)}
                        {unImplemented(TRACE_LOG)}
                        {unImplemented(SCAN_BLOCK)}
                        {unImplemented(MONEY)}
                        {unImplemented(CODE)}
                        {unImplemented(TIME)}
                        {unImplemented(CORE)}
                    </div>
                    <div className="btn-height-spacer"/>
                    <div>
                        {ice(ICE_PASSWORD)}
                        {unImplemented(ICE_FILM)}
                        {unImplemented(ICE_NETWALK)}
                        {unImplemented(ICE_WORD_SEARCH)}
                        {unImplemented(ICE_MAGIC_EYE)}
                        {unImplemented(ICE_PASSWORD_SEARCH)}
                        {unImplemented(ICE_ALTERNATE)}
                        {unImplemented(ICE_UNHACKABLE)}
                    </div>
                    <br/>


                </div>
            </div>
        );
    });
