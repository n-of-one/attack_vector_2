import React, {useEffect} from "react";
import {useSelector} from "react-redux";
import {UserDetails} from "./UserDetails";
import {GenericUserRootState} from "./UserReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {currentUser} from "../user/CurrentUser";


export const Me = () => {

    useEffect(() => {
        webSocketConnection.send("/user/select", currentUser.id)
    }, [])

    const user = useSelector((state: GenericUserRootState) => state.currentUser)

    return <div className="row">
        <div className="col-lg-6">
            <div className="row">
                <div className="col-lg-12">
                    <span className="text"><strong>🜁 Verdant OS 🜃</strong></span>
                </div>
            </div>
        </div>
        <div className="row">
            <div className="col-lg-4">
            </div>

            <div className="col-lg-4">
                <div className="text">
                    <br/>
                    <UserDetails user={user}/>
                </div>
            </div>
            <div className="col-lg-4">
            </div>
        </div>
    </div>
}