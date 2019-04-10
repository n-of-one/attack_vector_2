import React from 'react';
import {connect} from "react-redux";
import MenuBar from "../../common/component/MenuBar";
import {SCAN} from "../ScanActions";
import Terminal from "../../common/component/terminal/Terminal";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        scan: (siteName) => {
            dispatch({action: SCAN, siteName: siteName})
        }
    }
};
let mapStateToProps = (state) => {
    return {
        terminal: state.terminal,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({terminal}) => {

        document.body.style.backgroundColor = "#222222";

        return (
            <span>
            
            <div className="container">
                <div className="row">
                    <div className="col-lg-2">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 backgroundLight">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 rightPane">
                        <span className="text">Site: </span>
                    </div>
                </div>

                <div className="row">
                    <div className="col-lg-2">
                    </div>
                    <div className="col-lg-5">
                        <Terminal terminal={terminal}/>
                    </div>
                    <div className="col-lg-5 rightPane">
                        <div className="">
                            <canvas id="canvas" className="siteMap"/>
                        </div>
                    </div>
                </div>

            </div>
            <MenuBar/>
        </span>

        );
    });
