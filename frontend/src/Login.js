import React, {Component} from 'react';
import {post} from "./common/RestClient";

class Login extends Component {

    constructor(props) {
        super(props);
        this.loginFunction = props.loginFunction;
        this.state = {
            name: "",
            password: "",
            message: this.props.message};

        this.names = [
            "Obsidian", "Paradox", "Shade_zero", "_eternity_", "BoltBishop", "CryptoLaw", "Moonshine",
            "Angler", "N1X", "Face.dread", "-=Silver=-", "C_H_I_E_F", ".Specter.", "Stalker",
        ];
    }

    onNameChange(value) {
        this.setState({name: value});
    }

    onPasswordChange(value) {
        this.setState({password: value});
    }

    login(event) {
        event.preventDefault();
        const loginInput = {name: this.state.name, password: this.state.password};
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
    }

    renderName = (name) => {
        const that = this;
        return (
            <div className="row" key={name}>
                <div className="col-lg-offset-2 col-lg-5">
                    <div className="text">
                        <a href="#" onClick={
                            (event) => {
                                that.onNameChange(name);
                                return false;
                                event.preventDefault();
                            }} >{name}</a>
                    </div>
                </div>
            </div>
        )
    };

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
                                <div className="col-sm-offset-2 col-sm-10 text">
                                    <p>&nbsp;{this.state.message}</p>
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="inputEmail3" className="col-sm-2 control-label text">Handle</label>
                                <div className="col-sm-10">
                                    <input type="text" className="form-control" id="handle" placeholder="" autoFocus={true}
                                           onChange={(event) => {
                                               this.onNameChange(event.target.value)
                                           }}
                                           value={this.state.name} />
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="inputPassword3" className="col-sm-2 control-label text">Password</label>
                                <div className="col-sm-10">
                                    <input type="password" className="form-control" id="password" placeholder="Password"
                                           onChange={(event) => {
                                               this.onPasswordChange(event.target.value)
                                           }}
                                           value={this.state.password}/>
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
                {this.names.map(this.renderName)}
            </div>
        );
    }

}

export default Login;