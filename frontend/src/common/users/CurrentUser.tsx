import React, {useEffect} from "react";
import {useSelector} from "react-redux";
import {UserDetails} from "./UserDetails";
import {GenericUserRootState} from "./UserReducer";
import {webSocketConnection} from "../WebSocketConnection";
import {currentUser} from "../CurrentUser";


export const CurrentUser = () => {

    useEffect(() => {
        webSocketConnection.send("/av/user/select", currentUser.id)
    }, [])

    const user = useSelector((state: GenericUserRootState) => state.currentUser)

    return <div className="row">
        <div className="col-lg-6">
            <div className="row">
                <div className="col-lg-12">
                    <span className="text"><strong>🜁 Verdant OS 🜃</strong></span>
                </div>
            </div>
            <div className="row">
                <div className="col-lg-12">
                    <div className="text">
                        <br/>
                        <UserDetails user={user} />
                    </div>
                </div>
            </div>
        </div>
        <div className="col-lg-6">
            <div className="row">
                <div className="col-lg-12">
                    <span className="text"></span>
                </div>
            </div>
        </div>
    </div>
}