import React from "react";
import {useSelector} from "react-redux";
import {Page} from "../../../../common/menu/pageReducer";
import {ForceDisconnected} from "../../../../common/component/ForceDisconnected";
import {JigsawRootState} from "../reducer/JigsawRootReducer";
import {IceHackerPresence} from "../../common/IceHackerPresence";
import {JigsawHome} from "./JigsawHome";

export const JigsawContainer = () => {

    const currentPage: Page = useSelector((state: JigsawRootState) => state.currentPage)
    if (currentPage === Page.FORCE_DISCONNECT) return <ForceDisconnected/>

    return (
        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-2">
                    <div className="row">
                        <div className="col-lg-12">
                            <IceHackerPresence blocked={[]}/>
                        </div>
                    </div>
                </div>
                <div className="col-lg-10">
                    <JigsawHome/>
                </div>
            </div>
        </div>
    )
}
