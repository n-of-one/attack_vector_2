import React, {ReactElement} from 'react'
import Cookies from 'js-cookie'
import userAuthorizations from "./UserAuthorizations";


interface Props {
    requires?: string
    anyOf?: string[]
    children: ReactElement
}

export const RequiresRole = (props: Props) => {

    if (!userAuthorizations.authenticated) {
        document.location.href = `/login?next=${document.location.href}`
        return <></>
    }

    if (isAllowed(props)) {
        return (
            <>{props.children}</>
        )
    } else {
        return (
            <div className="text">
                You are logged in with an account that does not have access to this page. Please <a href="/login">Login</a> with another account.<br/>
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


