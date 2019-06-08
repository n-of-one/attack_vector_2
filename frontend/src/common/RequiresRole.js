import React, {Component} from 'react';
import Cookies from 'js-cookie'

class RequiresRole extends Component {

    constructor(props) {
        super(props);
        this.props = props;
        let jwt = Cookies.get("jwt");
        const authenticated = jwt !== undefined;
        let roles = [];
        const cookieRoles = Cookies.get("roles");
        if (cookieRoles) {
            roles = cookieRoles.split("|");
        }
        this.state = {
            authenticated: authenticated,
            roles: roles
        };
    }

    render() {
        if (!this.state.authenticated) {
            document.location.href = `/login?next=${document.location.href}`;
            return
        }
        if (this.state.roles.includes(this.props.requires)) {
            return (
                <div>
                    {this.props.children}
                </div>
            )
        }
        else {
            return (
                <div className="text">
                    You are not logged in with an account that has access to this page. Please <a href="/login">Login</a> with another account.<br />
                    <br />
                    Required role: {this.props.requires}
                </div>
            )
        }
    }
}


export default RequiresRole;