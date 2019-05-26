import React from 'react';
import {connect} from "react-redux";

const mapDispatchToProps = (dispatch) => {
    return {
        add: (type, nodeId) => {dispatch({type:"ADD_SERVICE", serviceType: type, nodeId: nodeId})},
    }
};

let mapStateToProps = (state) => {
    return { state: state};
};



export default connect(mapStateToProps, mapDispatchToProps)(
    ({add}) => {

        const regular = (type, glyphicon) => {
            return service(type, glyphicon, "btn btn-info btn-spaced")
        };

        const ice = (type, glyphicon) => {
            return service(type, glyphicon, "btn btn-primary btn-spaced")
        };

        const service = (type, glyphicon, enclosingClassName) => {
            const className = "glyphicon " + glyphicon;
            return (
                <div className={enclosingClassName} onClick={() => add(type)}>
                    <span className={className} />
                </div>
            );
        };
        return (
            <div className="row">
                <div className="col-lg-12 darkWell">
                    <br/>
                    <div>
                        { regular("text", "glyphicon-file") }
                        { regular("picture", "glyphicon-picture") }
                        { regular("link", "glyphicon-link") }
                        { regular("tracer", "glyphicon-transfer") }
                        { regular("tracelog", "glyphicon-erase") }
                        { regular("scanblock", "glyphicon-magnet") }
                        { regular("money", "glyphicon-usd") }
                        { regular("code", "glyphicon-ok-circle") }
                        { regular("time", "glyphicon-time") }
                        { regular("core", "glyphicon-th-large") }

                    </div>
                    <br/>
                    <div>
                        { ice("password", "glyphicon-console") }
                        { ice("film", "glyphicon-film") }
                        { ice("netwalk", "glyphicon-qrcode") }
                        { ice("wordsearch", "glyphicon-th") }
                        { ice("magic-eye", "glyphicon-eye-close") }
                        { ice("password-search", "glyphicon-tasks") }
                        { ice("alternate", "glyphicon-star") }
                        { ice("unhackable", "glyphicon-ban-circle") }
                    </div>
                    <br/>


                </div>
            </div>
        );
    });
