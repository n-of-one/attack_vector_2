import React from "react";
import {useSelector} from "react-redux";
import {Page} from "../../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../../common/component/ForceDisconnected";
import {SweeperRootState} from "../reducer/SweeperRootReducer";
import {IceHackerPresence} from "../../common/IceHackerPresence";
import {SweeperHome} from "./SweeperHome";

export const SweeperContainer = () => {

    const blockedUserIds = useSelector((state: SweeperRootState) => state.ui.blockedUserIds)
    const currentPage: Page = useSelector((state: SweeperRootState) =>  state.currentPage)
    if (currentPage === Page.FORCE_DISCONNECT) return <ForceDisconnected/>



    return (
        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-2">
                    <div className="row">
                        <div className="col-lg-12">
                            <IceHackerPresence blocked={blockedUserIds}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">&nbsp;
                            {/*<Terminal terminalState={messageTerminal} height="300px"/>*/}
                        </div>
                    </div>
                </div>
                <div className="col-lg-10">
                    <SweeperHome/>
                </div>
            </div>
        </div>
    )
}
