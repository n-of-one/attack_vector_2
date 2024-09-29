import {LoggedOut} from "./LoggedOut";
import {DevLogin} from "./DevLogin";
import {GoogleAuth} from "./GoogleAuth";
import React from "react";
import {LarpType} from "../common/Larp";

export const LoginRouter = () => {

    const []

    if (this.type === LarpType.FRONTIER) {
        return <LoggedOut/>
    }
    if (this.type === LarpType.LOCAL_DEV) {
        return <DevLogin/>
    }
    return <GoogleAuth/>


}
