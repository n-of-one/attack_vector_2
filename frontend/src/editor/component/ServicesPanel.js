import React from 'react';
import {connect} from "react-redux";
import {ADD_SERVICE} from "../EditorActions";

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

        const regular = (type, glyphicon) => {
            return service(type, glyphicon, "btn btn-info btn-spaced")
        };

        const ice = (type, glyphicon) => {
            return service(type, glyphicon, "btn btn-primary btn-spaced")
        };

        const service = (type, glyphicon, enclosingClassName) => {
            const className = "glyphicon " + glyphicon;
            return (
                <div className={enclosingClassName} onClick={() => add(type, currentNodeId)}>
                    <span className={className} />
                </div>
            );
        };
        return (
            <div className="row">
                <div className="col-lg-12 darkWell">
                    <br/>
                    <div>
                        { regular("TEXT", "glyphicon-file") }
                        { regular("PICTURE", "glyphicon-picture") }
                        { regular("LINK", "glyphicon-link") }
                        { regular("TRACER", "glyphicon-transfer") }
                        { regular("TRACELOG", "glyphicon-erase") }
                        { regular("SCANBLOCK", "glyphicon-magnet") }
                        { regular("MONEY", "glyphicon-usd") }
                        { regular("CODE", "glyphicon-ok-circle") }
                        { regular("TIME", "glyphicon-time") }
                        { regular("CORE", "glyphicon-th-large") }
                    </div>
                    <br/>
                    <div>
                        { ice("PASSWORD", "glyphicon-console") }
                        { ice("FILM", "glyphicon-film") }
                        { ice("NETWALK", "glyphicon-qrcode") }
                        { ice("WORDSEARCH", "glyphicon-th") }
                        { ice("MAGIC-eye", "glyphicon-eye-close") }
                        { ice("PASSWORD-search", "glyphicon-tasks") }
                        { ice("ALTERNATE", "glyphicon-star") }
                        { ice("UNHACKABLE", "glyphicon-ban-circle") }
                    </div>
                    <br/>


                </div>
            </div>
        );
    });
