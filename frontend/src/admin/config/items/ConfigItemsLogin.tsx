import React from "react"
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemLoginPath = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Login: path" value={props.value} item={ConfigItem.LOGIN_PATH}/>
            <small className="form-text text-muted">The path used for login.<br/><br/>
                Default: /login &nbsp; (this is for using Google login)<br/><br/>
                Alternatives:<br/>
                <ul>
                    <li>/devLogin &nbsp; (use during development, one click login without passwords)</li>
                    <li>/login-frontier &nbsp; (used for Frontier Larp SSO)</li>
                </ul>
                General advice: do not change this.<br/><br/>
                This is used to direct login to a page specific to your larp (instead of google login). This must be custom
                built for each larp.</small><br/><br/>
        </>
    )
}

export const ConfigItemLoginPassword = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Login: password" value={props.value} item={ConfigItem.LOGIN_PASSWORD}/>
            <small className="form-text text-muted">The single password used for GMs and Admins.<br/><br/>
                Default: (empty)<br/><br/>
                General advice: set this to a strong password that is not used anywhere else.<br/><br/>
                Attack Vector uses a single password in order to discourage using personal passwords on this site.
                This password can be used for Admin or GM to log in, if they don't have a personal account. </small><br/><br/>
            {checkPassword(props.value)}
        </>
    )
}

const checkPassword = (password: string) => {
    if (password === "") {
        return <small className="form-text text-muted"><span className="text-danger">Warning:</span> the current value is empty, meaning that anyone can log in
            as anyone. This is only safe for a development environment.</small>
    }
    if (password.length < 8) {
        return <small className="form-text text-muted"><span className="text-danger">Warning:</span> the current value is too short, please use at least 8
            characters, preferably 12 or more.</small>
    }
    return <></>
}

export const ConfigItemLoginGoogleClientId = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Login: Google Client ID" value={props.value} item={ConfigItem.LOGIN_GOOGLE_CLIENT_ID}/>
            <small className="form-text text-muted">The google client ID.<br/><br/>
                Default: (empty)<br/><br/>
                This is the (non secret) google client ID. If you want to use Google login, you must enter this
                value. It looks like this: <span className="text-bg-secondary text-black">(numbers)-(letters).apps.googleusercontent.com</span><br/><br/>
                {/* eslint-disable-next-line jsx-a11y/anchor-has-content */}
                Find your google client IDs in the list of "OAuth 2.0 Client IDs" on this page: <a
                    href="https://console.cloud.google.com/apis/credentials" target="_blank" rel="noreferrer" style={{textDecoration: "none"}}><>here</>
                </a>.</small><br/><br/>
        </>
    )
}
