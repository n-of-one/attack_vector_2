import React, {Component} from 'react';

class RequiresRole extends Component {

    constructor(props) {
        super(props);
        this.props = props;
        const authenticated = localStorage.getItem('token') != null;
        this.state = {authenticated: authenticated};
    }

    logout() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
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