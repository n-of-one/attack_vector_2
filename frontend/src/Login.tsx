import React, {FormEvent, useState} from 'react'
import {post} from "./common/RestClient"

interface SimpleLogin {
    name: string,
    login: string
}

const simpleLogins = [
    {name: "[Corne] Stalker", login: "Stalker"},
    {name: "[Josh] Shade_zero", login: "Shade_zero"},
    {name: "[Rob] Paradox", login: "Paradox"},
    {name: "[Sander] Angler", login: "Angler"},
    {name: "[Silvester] _eternity_", login: "_eternity_"},
    {name: "[Thijs] C_H_I_E_F", login: "C_H_I_E_F"},
    {name: "[Verik] -=Silver=-", login: "-=Silver=-"},
    {name: "*unclaimed* Obsidian", login: "Obsidian"},
    {name: "*unclaimed* BoltBishop", login: "BoltBishop"},
    {name: "*unclaimed* CryptoLaw", login: "CryptoLaw"},
    {name: "*unclaimed* Moonshine", login: "Moonshine"},
    {name: "*unclaimed* N1X", login: "N1X"},
    {name: "*unclaimed* Face.dread", login: "Face.dread"},
    {name: "*unclaimed* .Specter.", login: ".Specter."},
]

export const Login = () =>  {

    const [name, setName] = useState("")
    const [password, setPassword] = useState("")
    const [message, setMessage] = useState("")



    const onNameChange = (value: string) => {
        setName(value)
    }

    const onPasswordChange = (value: string) => {
        setPassword(value)
    }

    const submit = (event: FormEvent) => {
        event.preventDefault()
        login(name, password)
    }


    const login = (name: string, password: string) => {
        const loginInput = {name: name, password: password}
        setMessage("Logging in")

        post({
            url: "/api/login",
            body: loginInput,
            ok: ({success, message} : {success: boolean, message: string}) => {
                if (success) {
                    const search = document.location.search
                    if (search.length <= 5) {
                        document.location.href = "/"
                    } else {
                        document.location.href = search.substring(search.indexOf('=') + 1)
                    }
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


    const renderName = (simpleLogin: SimpleLogin) => {
        return (
            <div className="row" key={simpleLogin.login}>
                <div className="col-lg-offset-2 col-lg-5">
                    <div className="text">
                        {/*eslint-disable-next-line*/}
                        <a href="#" onClick={
                            (event) => {
                                login(simpleLogin.login, "")
                                event.preventDefault()
                                return false
                            }}>{simpleLogin.name}</a>
                    </div>
                </div>
            </div>
        )
    }

    return (
        <div className="container" data-bs-theme="dark">
            <br/>
            <br/>
            <br/>
            <br/>
            <div className="row">

                <div className="col-lg-offset-2 col-lg-5">
                    <form className="form-horizontal" onSubmit={(event) => submit(event)}>
                        <div className="form-group">
                            <div className="col-sm-offset-2 col-sm-10 text">
                                <p>&nbsp;{message}</p>
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="inputEmail3" className="col-sm-2 control-label text">Handle</label>
                            <div className="col-sm-10">
                                <input type="text" className="form-control" id="handle" placeholder="" autoFocus={true}
                                       onChange={(event) => {
                                           onNameChange(event.target.value)
                                       }}
                                       value={name}/>
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="inputPassword3" className="col-sm-2 control-label text">Password</label>
                            <div className="col-sm-10">
                                <input type="password" className="form-control" id="password" placeholder="Password"
                                       onChange={(event) => {
                                           onPasswordChange(event.target.value)
                                       }}
                                       value={password}/>
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="col-sm-offset-2 col-sm-10">
                                <button type="submit" className="btn btn-default">Sign in</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <br/>
            <br/>
            <br/>
            {simpleLogins.map(renderName)}
        </div>
    )

}
