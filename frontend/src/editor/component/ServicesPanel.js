import React from 'react';
import {connect} from "react-redux";
import {ADD_SERVICE} from "../EditorActions";
import {
    CODE, CORE, ICE_ALTERNATE, ICE_FILM, ICE_MAGIC_EYE, ICE_NETWALK, ICE_PASSWORD, ICE_PASSWORD_SEARCH,
    ICE_UNHACKABLE, ICE_WORD_SEARCH, LINK, MONEY, PICTURE, SCAN_BLOCK, TEXT, TIME, TRACE_LOG, TRACER
} from "./service/ServiceTypes";
import Glyphicon from "../../common/component/Glyphicon";

const mapDispatchToProps = (dispatch) => {
    return {
        add: (type, nodeId, implemented) => {
            if (implemented && nodeId != null) {
                dispatch({type: ADD_SERVICE, serviceType: type, nodeId: nodeId})
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
                        {regular(PICTURE)}
                        {regular(LINK)}
                        {regular(TRACER)}
                        {regular(TRACE_LOG)}
                        {regular(SCAN_BLOCK)}
                        {regular(MONEY)}
                        {regular(CODE)}
                        {regular(TIME)}
                        {regular(CORE)}
                    </div>
                    <br/>
                    <div>
                        {ice(ICE_PASSWORD)}
                        {ice(ICE_FILM)}
                        {ice(ICE_NETWALK)}
                        {ice(ICE_WORD_SEARCH)}
                        {ice(ICE_MAGIC_EYE)}
                        {ice(ICE_PASSWORD_SEARCH)}
                        {ice(ICE_ALTERNATE)}
                        {ice(ICE_UNHACKABLE)}
                    </div>
                    <br/>


                </div>
            </div>
        );
    });
