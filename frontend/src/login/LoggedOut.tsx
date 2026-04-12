import React, {useEffect} from "react";
import {Banner} from "./Banner";
import Cookies from "js-cookie";

export const LoggedOut = () => {
    Cookies.remove("jwt")
    Cookies.remove("type")
    Cookies.remove("roles")
    Cookies.remove("userName")

    useEffect(() => {
        fetch("/localLogout")
    }, []);

    return (
        <div className="container" data-bs-theme="dark">
            <Banner hiddenAdminLogin={true}/>
            <div className="row">
                <div className="d-flex justify-content-center">
                    <button className="btn btn-info" style={{opacity: 0.7}} onClick={() => {
                        window.location.href = "/redirectToLogin"
                    }}>Login
                    </button>
                </div>
            </div>
        </div>
    )
}
