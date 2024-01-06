import React, {FormEvent, useState} from 'react'
import {post} from "../common/server/RestClient"
import {Banner} from "./Banner";
import {TextSaveInput} from "../common/component/TextSaveInput";

export const AdminLogin = () => {

    const [name, setName] = useState("")
    const [password, setPassword] = useState("")
    const [message, setMessage] = useState("")

    const submit = (event: FormEvent) => {
        event.preventDefault()
        login(name, password)
    }

    const login = (name: string, password: string) => {
        const loginInput = {name: name, password: password}
        setMessage("Logging in")

        post({
            url: "/openapi/login",
            body: loginInput,
            ok: ({success, message}: { success: boolean, message: string }) => {
                if (success) {
                    document.location.href = "/"
                } else {
                    setMessage(message)
                }
            },
            notok: () => {
                setMessage("Connection to server failed, unable to continue.")
            },
            error: () => {
                setMessage("Connection to server failed, unable to continue.")
            }
        })
    }

    return (
        <div className="container" data-bs-theme="dark">
            <Banner/>
            <div className="row text">
                <div className="d-flex justify-content-center">
                    <h3>Admin login</h3>
                </div>
            </div>

            <br/>


            <div className="row text">
                <div className="d-flex justify-content-center">
                    <form onSubmit={(event) => submit(event)}>
                        <div className="row align-items-center">
                            <div className="col col-4">
                                <label htmlFor="name" className="col-form-label">Name</label>
                            </div>
                            <div className="col col-8">
                                <input type="text" id="name" className="form-control"
                                       placeholder="" autoFocus={true}
                                       onChange={(event) => {
                                           setName(event.target.value)
                                       }}
                                       value={name}/>
                            </div>
                        </div>

                        <div className="row align-items-center">
                            <div className="col col-4">
                                <label htmlFor="password" className="col-form-label">Password</label>
                            </div>
                            <div className="col col-8">
                                <input type="password" id="password" className="form-control"
                                       onChange={(event) => {
                                           setPassword(event.target.value)
                                       }}
                                       value={password}/>
                            </div>
                        </div>

                        <div className="row align-items-center">
                            <div className="col col-4"></div>
                            <div className="col col-8">
                                <button type="submit" className="btn btn-primary button-text">Sign in</button>
                            </div>
                        </div>
                        <br/>
                    </form>
                </div>
            </div>
            <br/>
            <div className="row text">
                <div className="d-flex justify-content-center">
                    {message}
                </div>
            </div>
            <br/>
            <div className="row text">
                <div className="d-flex justify-content-center">
                    <a href="/">Return to regular login</a>
                </div>
            </div>
        </div>
    )
}
