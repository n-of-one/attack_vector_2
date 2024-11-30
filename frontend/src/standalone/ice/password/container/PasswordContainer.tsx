import React from "react";
import {PasswordIceHome} from "./PasswordIceHome";
import {useSelector} from "react-redux";
import {PasswordRootState} from "../reducer/PasswordRootReducer";
import {ForceDisconnected} from "../../../../common/component/ForceDisconnected";
import {IceHackerPresence} from "../../common/IceHackerPresence";
import {Page} from "../../../../common/menu/pageReducer";

export const PasswordContainer = () => {

    const currentPage: Page =  useSelector((state: PasswordRootState) =>  state.currentPage)
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
                </div>
                <div className="col-lg-10">
                    <PasswordIceHome/>
                </div>
            </div>
        </div>
    )
}
