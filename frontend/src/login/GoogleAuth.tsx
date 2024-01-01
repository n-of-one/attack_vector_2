import React, {useEffect} from "react";
import {CredentialResponse, GoogleLogin, GoogleOAuthProvider} from "@react-oauth/google";

enum LoginState {
    LOADING_CLIENT_ID = "LOADING_CLIENT_ID",
    INACTIVE = "INACTIVE",
    ACTIVE = "ACTIVE",
    SUCCESS = "SUCCESS",
    ERROR = "ERROR",
}

export const GoogleAuth = () => {

    const [state, setState] = React.useState(LoginState.LOADING_CLIENT_ID)
    const [jwt, setJwt] = React.useState("")
    const [clientId, setClientId] = React.useState("")

    useEffect(() => {
        const fetchClientId = async () => {
            const response: Response = await fetch("/openapi/google/clientId")
            const text: string = await response.text()
            setClientId(text)
            setState(LoginState.INACTIVE)
        }
        fetchClientId().catch(() => {
            setState(LoginState.ERROR)
        })
    }, [])


    const onSuccess = (response: CredentialResponse) => {
        if (response.credential === undefined) {
            alert("Failed to login")
            return
        }
        setJwt(response.credential)
        setState(LoginState.SUCCESS)
    }

    const onError = () => {
        setState(LoginState.ERROR)
    }

    if (state === LoginState.LOADING_CLIENT_ID) {
        return <div className="text">Loading client id</div>
    }

    if (state === LoginState.ACTIVE) {

    }

        if (state === LoginState.ACTIVE) {
        return (<>
            <GoogleOAuthProvider clientId={clientId}>
                <div>
                    <GoogleLogin onSuccess={onSuccess} onError={onError}/>
                </div>
            </GoogleOAuthProvider>

        </>)
    }

    if (state === LoginState.SUCCESS) {
        setTimeout(() => {
            const element = document.getElementById("googleLoginForm") as HTMLFormElement
            element.submit()
        }, 100)
        return (<form method="post" action="http://localhost/login/google" id="googleLoginForm">
            <input type="hidden" name="jwt" value={jwt}/>
        </form>)
    }

    // state === LoginState.ERROR
    return (<div className="text">Login via Google failed. Please refresh your browser and try again.</div>)
}
