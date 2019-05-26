import React from 'react';
import {connect} from "react-redux";
import {ADD_SERVICE} from "../EditorActions";
import {
    ALTERNATE, CODE, CORE, FILM, LINK, MAGIC_EYE, MONEY, NETWALK, PASSWORD,
    PASSWORD_SEARCH, PICTURE, SCAN_BLOCK, TEXT, TIME, TRACE_LOG, TRACER, WORD_SEARCH, UNHACKABLE
} from "./service/ServiceTypes";
import Glyphicon from "../../common/component/Glyphicon";

const mapDispatchToProps = (dispatch) => {
    return {
        add: (type, nodeId) => {
            if (nodeId != null) {
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
            return service(type, "btn btn-info btn-spaced", true)
        };

        const ice = (type) => {
            return service(type, "btn btn-primary btn-spaced", true)
        };

        const unImplemented = (type) => {
            return service(type, "btn btn-grey btn-spaced", false)
        };

        const service = (type, enclosingClassName, implemented) => {
            return (
                <div className={enclosingClassName} onClick={() => {
                    if (implemented) {
                        add(type, currentNodeId)
                    }
                }}>
                    <Glyphicon type={type} size="18px"/>
                </div>
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
                    <br/>
                    <div>
                        {unImplemented(PASSWORD)}
                        {unImplemented(FILM)}
                        {unImplemented(NETWALK)}
                        {unImplemented(WORD_SEARCH)}
                        {unImplemented(MAGIC_EYE)}
                        {unImplemented(PASSWORD_SEARCH)}
                        {unImplemented(ALTERNATE)}
                        {unImplemented(UNHACKABLE)}
                    </div>
                    <br/>


                </div>
            </div>
        );
    });
