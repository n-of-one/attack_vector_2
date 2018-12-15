import React, {Component} from 'react';
import Login from "./Login";

class RequiresRole extends Component {

    constructor(props) {
        super(props);
        this.props = props;
        const token = localStorage.getItem('token');
        this.state = {
            message: "hello",
            authenticated: (token != null),
        };
    }

    login(name, password) {
        const loginInput = {name: name, password: password};
        this.setState({message: "Logging in"});
        fetch("/api/login/", {
            method: "POST",
            body: JSON.stringify(loginInput),
            headers: {"Content-Type": "application/json; charset=utf-8",}
        })
            .then(response => {
                if (response.ok) {
                    response.text().then(data => {
                        const {token, message, role} = JSON.parse(data);
                        if (token) {
                            localStorage.setItem("token", token);
                            localStorage.setItem("role", role);
                            this.setState({authenticated: true})
                        }
                        else {
                            this.setState({message: message})
                        }
                    });
                }
                else {
                    this.setState({message: "Connection to server failed, unable to continue."});
                }
            });


        // this.loginFunction(this.name, this.password);
    }


    logout() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        this.setState({authenticated: false});
    }

    render() {
        if (!this.state.authenticated) {
            return (
                <Login loginFunction={(name, password) => this.login(name, password)} message={this.state.message}/>);
        }
        else {
            return (
                <div style={{backgroundColor: "#FFFFFF"}}>
                    <button onClick={() => this.logout()}>Logout</button>
                    {this.props.children}
                </div>
            )
        }
    }
}

export default RequiresRole;