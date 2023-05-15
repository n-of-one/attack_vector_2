import React from "react";
import {SlowIceHome} from "./SlowIceHome";
import {useSelector} from "react-redux";
import {DISCONNECTED} from "../../../common/menu/pageReducer";
import {Disconnected} from "../../../common/component/Disconnected";
import {SlowIceRootState} from "../reducer/SlowIceRootReducer";
import {IceHackerPresence} from "../../common/IceHackerPresence";


export const SlowIceContainer = () => {

    const currentPage: string =  useSelector((state: SlowIceRootState) =>  state.currentPage)
    if (currentPage === DISCONNECTED) return <Disconnected/>

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
                    <SlowIceHome/>
                </div>
            </div>
        </div>
    )
}