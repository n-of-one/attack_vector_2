import {User} from "../../users/CurrentUserReducer";
import {CreditsIcon} from "../../component/icon/CreditsIcon";
import React from "react";

export const CurrentCredits = ({user}: { user: User }) => {
    return <>
        Script credit balance: <span className="text-info">{user.hacker?.scriptCredits}<CreditsIcon/></span>
    </>
}
