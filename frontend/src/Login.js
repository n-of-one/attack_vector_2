import React, {Component} from 'react';
import {post} from "./common/RestClient";

class Login extends Component {

    constructor(props) {
        super(props);
        this.loginFunction = props.loginFunction;
        this.state = {message: this.props.message};

        this.name = "";
        this.password = "";
    }

    onNameChange(value) {
        this.name = value;
    }

    onPasswordChange(value) {
        this.password = value;
    }

    login(event) {
        event.preventDefault();
        const loginInput = {name: this.name, password: this.password};
        this.setState({message: "Logging in"});

        post({
            url: "/api/login",
            body: loginInput,
            ok: ({success, message}) => {
                if (success) {
                    const search = document.location.search;
                    if (search.length <= 5) {
                        document.location.href = "/";
                    }
                    else {
                        const next = search.substring(search.indexOf('=') + 1);
                        document.location.href = next;
                    }
                }
                else {
                    this.setState({message: message});
                }
            },
            notok: () => {
                this.setState({message: "Connection to server failed, unable to continue."});
            }
        });


        // fetch("/api/login", {
        //         credentials: 'include',
        //         method: "POST",
        //         headers: { "Content-Type": "application/json" },
        //         body: JSON.stringify(loginInput),
        //     }
        // ).then(response => {
        //     if (response.ok) {
        //         response.text().then(data => {
        //             const {success, message} = JSON.parse(data);
        //             if (success) {
        //                 const search = document.location.search;
        //                 if (search.length <= 5) {
        //                     document.location.href = "/";
        //                 }
        //                 else {
        //                     const next = search.substring(search.indexOf('=') + 1);
        //                     document.location.href = next;
        //                 }
        //             }
        //             else {
        //                 this.setState({message: message})
        //             }
        //         });
        //     }
        //     else {
        //
        //     }
        // }
        // ).catch(error => {
        //     console.log(error);
        // });
    }

    render() {
        return (
            <div className="container">
                <br/>
                <br/>
                <br/>
                <br/>
                <div className="row">

                    <div className="col-lg-offset-2 col-lg-5">
                        <form className="form-horizontal" onSubmit={(event) => this.login(event)}>
                            <div className="form-group">
                                <div className="col-sm-offset-2 col-sm-10">
                                    <p>&nbsp;{this.state.message}</p>
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="inputEmail3" className="col-sm-2 control-label">Handle</label>
                                <div className="col-sm-10">
                                    <input type="text" className="form-control" id="handle" placeholder="" autofocus="true"
                                           onChange={(event) => {
                                               this.onNameChange(event.target.value)
                                           }}/>
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="inputPassword3" className="col-sm-2 control-label">Password</label>
                                <div className="col-sm-10">
                                    <input type="password" className="form-control" id="password" placeholder="Password"
                                           onChange={(event) => {
                                               this.onPasswordChange(event.target.value)
                                           }}/>
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
            </div>
        );
    }

}

export default Login;