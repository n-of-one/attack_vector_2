import React from "react";
import {PasswordIceHome} from "./PasswordIceHome";
import {Terminal} from "../../../common/terminal/Terminal";
import {useSelector} from "react-redux";
import {PasswordRootState} from "../reducer/PasswordRootReducer";
import {FORCE_DISCONNECT} from "../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../common/component/ForceDisconnected";
import {IceHackerPresence} from "../../common/IceHackerPresence";

export const PasswordContainer = () => {

    const chatTerminal = useSelector( (state: PasswordRootState) => state.chatTerminal )

    const currentPage: string =  useSelector((state: PasswordRootState) =>  state.currentPage)
    if (currentPage === FORCE_DISCONNECT) return <ForceDisconnected/>

    return (
        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-2">
                    <div className="row">
                        <div className="col-lg-12">
                            <IceHackerPresence />
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