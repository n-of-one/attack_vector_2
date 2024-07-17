import React, {FormEvent, useState} from 'react'
import {restPost} from "../common/server/RestClient"
import {Banner} from "./Banner";


export const redirect = () => {
    const next = new URLSearchParams(window.location.search).get("next")
    if (next === null) {
        document.location.href = "/"
    } else {
        const allowedRedirect = /^[a-zA-Z0-9/_\-=]{1,200}$/ // allow only a-z A-Z 0-9 / _ - =   the URL-base64 characters and /
        if (allowedRedirect.test(next)) {
            document.location.href = next
        } else {
            document.location.href = "/"
        }
    }
}


export const DevLogin = () => {

    const [name, setName] = useState("")
    const [message, setMessage] = useState("")


    const onNameChange = (value: string) => {
        setName(value)
    }

    const submit = (event: FormEvent) => {
        event.preventDefault()
        login(name)
    }


    const login = (name: string) => {
        const loginInput = {name: name, password: "not-needed-for-dev"}
        setMessage("Logging in")

        restPost({
            url: "/openapi/login",
            body: loginInput,
            ok: ({success, message}: { success: boolean, message: string }) => {
                if (success) {
                    redirect()
                } else {
                    setMessage(message)
                }
            },
            error: () => {
                setMessage("Connection to server failed, unable to continue.")
            }
        })
    }

    const renderName = (name: string) => {
        return (<div className="row text">
                <div className="d-flex justify-content-center">

                    <a href="src#" onClick={
                        (event) => {
                            login(name)
                            event.preventDefault()
                            return false
                        }}>{name}</a>
                </div>
            </div>
        )
    }

    return (
        <div className="container" data-bs-theme="dark">
            <Banner/>
            <div className="row text">
                <div className="d-flex justify-content-center">
                    <strong>Login as</strong><br/>
                </div>
            </div>
            <br/>
            <form onSubmit={(event) => submit(event)}>
                <div className="row text">
                    <div className="d-flex justify-content-center">
                        <input type="text" className="form-control" id="handle" placeholder="" autoFocus={true}
                               onChange={(event) => {
                                   onNameChange(event.target.value)
                               }}
                               value={name}
                               style={{width: "200px"}}
                        />
                    </div>
                </div>
            </form>
            <br/>
            {["hacker", "Stalker", "Paradox"].map(renderName)}
            <br/>
            {["gm"].map(renderName)}
            <br/>
            {["admin"].map(renderName)}
            <br />
            <div className="row text">
                <div className="d-flex justify-content-center">
                    <strong>{message}</strong><br/>
                </div>
            </div>

        </div>
    )
}
