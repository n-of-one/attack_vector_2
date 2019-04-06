import React, {Component} from 'react';
import Cookies from 'js-cookie'

class RequiresRole extends Component {

    constructor(props) {
        super(props);
        this.props = props;
        let jwt = Cookies.get("jwt");
        const authenticated = jwt !== undefined;
        this.state = {authenticated: authenticated};
    }

    logout() {
        Cookies.remove("jwt");
        this.setState({authenticated: false});
    }

    render() {
        if (!this.state.authenticated) {
            document.location.href = `/login?next=${document.location.href}`;
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