import React from 'react';
import {connect} from "react-redux";
import MenuBar from "../../common/component/MenuBar";
import Terminal from "../../common/terminal/Terminal";
import {TERMINAL_RECEIVE} from "../../common/terminal/TerminalActions";
import ScanCanvasPanel from "./ScanCanvasPanel";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
    }
};
let mapStateToProps = (state) => {
    return {
        terminal: state.terminal,
    };
};



export default connect(mapStateToProps, mapDispatchToProps)(
    ({terminal}) => {

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
                    <div className="col-lg-2">&nbsp;
                    </div>
                    <div className="col-lg-5">
                        <Terminal terminal={terminal}/>
                    </div>
                    <div className="col-lg-5 rightPane">
                        <ScanCanvasPanel />
                    </div>
                </div>

            </div>
            <MenuBar/>
        </span>

        );
    });
