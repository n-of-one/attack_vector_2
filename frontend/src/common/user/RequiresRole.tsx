import React, {ReactElement} from 'react'
import userAuthorizations from "./UserAuthorizations";
import {larp} from "../Larp";


interface Props {
    requires?: string
    anyOf?: string[]
    children: ReactElement
}

export const RequiresRole = (props: Props) => {

    if (!userAuthorizations.authenticated) {
        window.location.href = `${larp.loginUrl}?next=${document.location.pathname}`
        return <></>
    }

    if (isAllowed(props)) {
        return (
            <>{props.children}</>
        )
    } else {
        return (
            <div className="text">
                You are logged in with an account that does not have access to this page. Please <a href={`/login?next=${document.location.pathname}`}>Login</a> with another account.<br/>
                <br/>
            </div>
        )
    }
}

const isAllowed= (props: Props): boolean => {
    if (props.requires) {
        return userAuthorizations.roles.includes(props.requires)
    }
    if (props.anyOf) {
        return props.anyOf.some(role => userAuthorizations.roles.includes(role))
    }
    return false
}


