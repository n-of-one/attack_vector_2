import React from "react";
import {useSelector} from "react-redux";
import {FORCE_DISCONNECT} from "../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../common/component/ForceDisconnected";
import {AuthAppRootState} from "../reducer/AuthAppRootReducer";
import {AuthAppHome} from "./AuthAppHome";

export const AuthAppContainer = () => {

    const currentPage: string = useSelector((state: AuthAppRootState) =>  state.currentPage)
    if (currentPage === FORCE_DISCONNECT) return <ForceDisconnected/>

    return (
        <div className="container-fluid" data-bs-theme="dark">
            <AuthAppHome/>
        </div>
    )
}
