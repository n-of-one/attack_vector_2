import React, {FormEvent, useState} from 'react'
import {post} from "../common/server/RestClient"

interface SimpleLogin {
    name: string,
    login: string
}

const simpleLogins = [
    {name: "[Corne]", login: "Stalker"},
    {name: "[Josh]", login: "Shade_zero"},
    {name: "[Rob]", login: "Paradox"},
    {name: "[Sander]", login: "Angler"},
    {name: "[Silvester]", login: "eternity"},
    {name: "[Thijs]", login: "C_H_I_E_F"},
    {name: "[Verik]", login: "Silver"},
    {name: "*unclaimed*", login: "Obsidian"},
    {name: "*unclaimed*", login: "BoltBishop"},
    {name: "*unclaimed*", login: "CryptoLaw"},
    {name: "*unclaimed*", login: "Moonshine"},
    {name: "*unclaimed*", login: "N1X"},
    {name: "*unclaimed*", login: "Face.dread"},
    {name: "*unclaimed*", login: "Specter"},
]

if (window.location.port === "3000") {
    simpleLogins.push({name: "[admin]", login: "Admin"})
    simpleLogins.push({name: "[gm]", login: "gm"})
}

const redirect = () => {
    const search = document.location.search
    if (search.length <= 5) {
        document.location.href = "/"
    } else {
        const next = search.substring(search.indexOf('next=') + 5)
        const allowedRedirect = /^[a-zA-Z0-9_/-]{1,20}$/g
        if (allowedRedirect.test(next)) {
            document.location.href = next
        }
        else {
            document.location.href = "/"
        }
    }
}


export const Login = () =>  {

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
        const loginInput = {name: name}
        setMessage("Logging in")

        post({
            url: "/api/login",
            body: loginInput,
            ok: ({success, message} : {success: boolean, message: string}) => {
                if (success) {
                    redirect()
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
                        <span>{simpleLogin.name} </span>
                        {/*eslint-disable-next-line*/}
                        <a href="src#" onClick={
                            (event) => {
                                login(simpleLogin.login)
                                event.preventDefault()
                                return false
                            }}>{simpleLogin.login}</a>
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
