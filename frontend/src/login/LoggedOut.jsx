import {larp} from "../common/Larp";
import React from "react";
import {Banner} from "./Banner";

export const LoggedOut = () => {
    return (
        <div className="container" data-bs-theme="dark">
            <Banner hiddenAdminLogin={true}/>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <button className="btn btn-info" style={{opacity: 0.7}} onClick={() => {
                        window.location.href = larp.loginUrl
                    }}>Login
                    </button>
                </div>
            </div>
        </div>
    )
}