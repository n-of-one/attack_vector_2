import React from "react";
import {WordSearchHome} from "./WordSearchHome";
import {useSelector} from "react-redux";
import {FORCE_DISCONNECT} from "../../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../../common/component/ForceDisconnected";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {IceHackerPresence} from "../../common/IceHackerPresence";


export const WordSearchContainer = () => {

    const currentPage: string =  useSelector((state: WordSearchRootState) =>  state.currentPage)
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
                    <WordSearchHome/>
                </div>
            </div>
        </div>
    )
}