import React from "react";
import {SlowIceHome} from "./SlowIceHome";


export const SlowIceContainer = () => {
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
                    <SlowIceHome/>
                </div>
            </div>
        </div>
    )
}