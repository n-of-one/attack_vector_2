import React, {Component} from 'react';


class Login extends Component {

    constructor(props) {
        super(props);
        this.loginFunction = props.loginFunction;
        this.message = props.message;

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
        this.loginFunction(this.name, this.password);
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
                                    <p>&nbsp;{this.props.message}</p>
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="inputEmail3" className="col-sm-2 control-label">Handle</label>
                                <div className="col-sm-10">
                                    <input type="text" className="form-control" id="handle" placeholder=""
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