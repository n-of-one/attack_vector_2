import React from "react";
import {TangleIceHome} from "./TangleIceHome";


export const TangleContainer = () => {
    return (
        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-2">
                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text">&nbsp;</span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">&nbsp;
                            {/*<Terminal terminalState={messageTerminal} height="300px"/>*/}
                        </div>
                    </div>
                </div>
                <div className="col-lg-10">
                    <TangleIceHome/>
                </div>
            </div>
        </div>
    )
}