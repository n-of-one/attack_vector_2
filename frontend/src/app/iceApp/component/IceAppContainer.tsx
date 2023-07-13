import React from "react";
import {useSelector} from "react-redux";
import {FORCE_DISCONNECT} from "../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../common/component/ForceDisconnected";
import {IceAppRootState} from "../reducer/IceAppRootReducer";
import {IceAppHome} from "./IceAppHome";

export const IceAppContainer = () => {

    const currentPage: string = useSelector((state: IceAppRootState) =>  state.currentPage)
    if (currentPage === FORCE_DISCONNECT) return <ForceDisconnected/>

    return (
        <div className="container-fluid" data-bs-theme="dark">
            <IceAppHome/>
        </div>
    )
}
