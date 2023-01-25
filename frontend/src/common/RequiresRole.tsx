import React, {ReactElement} from 'react'
import Cookies from 'js-cookie'


const jwt: string | undefined = Cookies.get("jwt")
const authenticated: boolean = jwt !== undefined
const cookieRoles: string | undefined = Cookies.get("roles")
const roles = (cookieRoles !== undefined) ? cookieRoles.split("|") : []

interface Props {
    requires: string
    children: ReactElement
}
export const RequiresRole = (props : Props) => {

        if (!authenticated) {
            document.location.href = `/login?next=${document.location.href}`
            return <></>
        }
        if (roles.includes(props.requires)) {
            return (
                <>{props.children}</>
            )
        }
        else {
            return (
                <div className="text">
                    You are not logged in with an account that has access to this page. Please <a href="/login">Login</a> with another account.<br />
                    <br />
                    Required role: {props.requires}
                </div>
            )
        }
    }


