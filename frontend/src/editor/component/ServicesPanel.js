import React from 'react';
import {connect} from "react-redux";
import {ADD_SERVICE} from "../EditorActions";
import {
    ALTERNATE, CODE, CORE, FILM, LINK, MAGIC_EYE, MONEY, NETWALK, PASSWORD,
    PASSWORD_SEARCH, PICTURE, SCAN_BLOCK, TEXT, TIME, TRACE_LOG, TRACER, WORD_SEARCH } from "./service/ServiceTypes";
import {UNHACKABLE} from "../../common/enums/NodeTypesNames";
import Glyphicon from "../../common/component/Glyphicon";

const mapDispatchToProps = (dispatch) => {
    return {
        add: (type, nodeId) => {
            if (nodeId != null) {
                dispatch({type:ADD_SERVICE, serviceType: type, nodeId: nodeId})
            }
        },
    }
};

let mapStateToProps = (state) => {
    return { state: state, currentNodeId: state.currentNodeId };
};


export default connect(mapStateToProps, mapDispatchToProps)(
    ({add, currentNodeId}) => {

        const regular = (type) => {
            return service(type, "btn btn-info btn-spaced")
        };

        const ice = (type) => {
            return service(type, "btn btn-primary btn-spaced")
        };

        const service = (type, enclosingClassName) => {
            return (
                <div className={enclosingClassName} onClick={() => add(type, currentNodeId)}>
                    <Glyphicon type={type} size="18px" />
                </div>
            );
        };
        return (
            <div className="row">
                <div className="col-lg-12 darkWell">
                    <br/>
                    <div>
                        { regular(TEXT) }
                        { regular(PICTURE) }
                        { regular(LINK) }
                        { regular(TRACER) }
                        { regular(TRACE_LOG) }
                        { regular(SCAN_BLOCK) }
                        { regular(MONEY) }
                        { regular(CODE) }
                        { regular(TIME) }
                        { regular(CORE) }
                    </div>
                    <br/>
                    <div>
                        { ice(PASSWORD) }
                        { ice(FILM) }
                        { ice(NETWALK) }
                        { ice(WORD_SEARCH) }
                        { ice(MAGIC_EYE) }
                        { ice(PASSWORD_SEARCH) }
                        { ice(ALTERNATE) }
                        { ice(UNHACKABLE) }
                    </div>
                    <br/>


                </div>
            </div>
        );
    });
