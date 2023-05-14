import React from "react";
import {NetwalkHome} from "./NetwalkHome";
import {useSelector} from "react-redux";
import {DISCONNECTED} from "../../../common/menu/pageReducer";
import {Disconnected} from "../../../common/component/Disconnected";
import {NetwalkRootState} from "../reducer/NetwalkRootReducer";

export const NetwalkContainer = () => {

    const currentPage: string = useSelector((state: NetwalkRootState) =>  state.currentPage)
    if (currentPage === DISCONNECTED) return <Disconnected/>

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
                    <NetwalkHome/>
                </div>
            </div>
        </div>
    )
}
