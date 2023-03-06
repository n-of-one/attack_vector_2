import React from "react";
import {PasswordIceHome} from "./PasswordIceHome";
import {Terminal} from "../../../common/terminal/Terminal";
import {useSelector} from "react-redux";
import {PasswordRootState} from "../PasswordRootReducer";

export const PasswordContainer = () => {

    const chatTerminal = useSelector( (state: PasswordRootState) => state.chatTerminal )

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
                            <Terminal terminalState={chatTerminal} height="300px"/>
                        </div>
                    </div>
                </div>
                <div className="col-lg-10">
                    <PasswordIceHome/>
                </div>
            </div>
        </div>
    )
}