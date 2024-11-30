import React from "react";
import {NetwalkHome} from "./NetwalkHome";
import {useSelector} from "react-redux";
import {ForceDisconnected} from "../../../../common/component/ForceDisconnected";
import {NetwalkRootState} from "../reducer/NetwalkRootReducer";
import {IceHackerPresence} from "../../common/IceHackerPresence";
import {Page} from "../../../../common/menu/pageReducer";

export const NetwalkContainer = () => {

    const currentPage: Page = useSelector((state: NetwalkRootState) =>  state.currentPage)
    if (currentPage === Page.FORCE_DISCONNECT) return <ForceDisconnected/>

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
                    <NetwalkHome/>
                </div>
            </div>
        </div>
    )
}
