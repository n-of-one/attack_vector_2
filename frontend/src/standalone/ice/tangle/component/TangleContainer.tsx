import React from "react";
import {TangleIceHome} from "./TangleIceHome";
import {useSelector} from "react-redux";
import {FORCE_DISCONNECT} from "../../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../../common/component/ForceDisconnected";
import {TangleRootState} from "../reducer/TangleRootReducer";
import {IceHackerPresence} from "../../common/IceHackerPresence";


export const TangleContainer = () => {

    const currentPage: string =  useSelector((state: TangleRootState) =>  state.currentPage)
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