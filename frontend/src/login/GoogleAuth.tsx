import React, {useEffect, useState} from "react";
import {CredentialResponse, GoogleLogin, GoogleOAuthProvider} from "@react-oauth/google";
import {Banner} from "./Banner";
import {restGet, restPost} from "../common/server/RestClient";
import {redirect} from "./DevLogin";

enum LoginState {
    LOADING_CLIENT_ID = "LOADING_CLIENT_ID",
    INACTIVE = "INACTIVE",
    ACTIVE = "ACTIVE",
    ERROR = "ERROR",
}

export const GoogleAuth = () => {

    const [state, setState] = React.useState(LoginState.LOADING_CLIENT_ID)
    const [clientId, setClientId] = React.useState("")
    const [message, setMessage] = useState("")

    useEffect(() => {
        restGet({
            url: "/openapi/google/clientId",
            ok: ({clientId}: { clientId: string }) => {
                setClientId(clientId)
                setState(LoginState.INACTIVE)
            },
            error: () => {
                setState(LoginState.ERROR)
                setMessage("Google login configuration could not be loaded. Is the backend running?")
            }
        });

    }, [])


    const onSuccess = (response: CredentialResponse) => {
        if (response.credential === undefined) {
            alert("Failed to login")
            return
        }

        restPost({
            url: "/openapi/login/google",
            body: {"jwt": response.credential},
            ok: ({success, message}: { success: boolean, message: string }) => {
                if (success) {
                    redirect()
                } else {
                    setState(LoginState.ERROR)
                    setMessage(message)
                }
            },
            error: () => {
                setState(LoginState.ERROR)
                setMessage("Connection to server failed, unable to continue.")
            }
        })


    }

    const onError = () => {
        setMessage("Authentication failed with Google.")
        setState(LoginState.ERROR)
    }

    if (state === LoginState.LOADING_CLIENT_ID) {
        return <div className="text">Loading client id</div>
    }

    if (state === LoginState.INACTIVE) {
        return (<div className="container" data-bs-theme="dark">
            <Banner hiddenAdminLogin={true}/>
            <div className="row text">
                <div className="d-flex justify-content-center">
                    <br/>
                    <button className={"btn btn-primary button-text"} onClick={() => {
                        setState(LoginState.ACTIVE)
                    }}>Login with Google
                    </button>
                </div>
            </div>
            <br/>

        </div>)

    }

    if (state === LoginState.ACTIVE) {
        return (<div className="container" data-bs-theme="dark">
            <Banner hiddenAdminLogin={true}/>
            <div className="row text">
                <div className="d-flex justify-content-center">
                    <br/>

                    <GoogleOAuthProvider clientId={clientId}>
                        <div>
                            <GoogleLogin onSuccess={onSuccess} onError={onError}/>
                        </div>
                    </GoogleOAuthProvider>
                </div>
            </div>
            <br/>
            <div className="row text dark">
                <div className="d-flex justify-content-center">
                    The reason for not immediately showing the Google login button<br/> is to prevent Google from tracking you when it is not necessary.
                </div>
            </div>
        </div>)

    }

    // state === LoginState.ERROR
    return (<div className="container" data-bs-theme="dark">
        <Banner hiddenAdminLogin={true}/>
        <div className="row text">
            <div className="d-flex justify-content-center">
                <h5>There was a problem with Google login</h5>
            </div>
        </div>
        <div className="row text">
            <div className="d-flex justify-content-center">
                <p>
                {message}
                </p>
            </div>
        </div>
        <br/>
    </div>)

}

