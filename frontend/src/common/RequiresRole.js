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

    render() {
        if (!this.state.authenticated) {
            document.location.href = `/login?next=${document.location.href}`;
        }
        else {
            return (
                <div>
                    {this.props.children}
                </div>
            )
        }
    }
}


export default RequiresRole;