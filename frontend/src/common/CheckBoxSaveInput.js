import React, {Component} from 'react';


export default class CheckboxSaveInput extends Component {

    state = {
        initialized: false,
    };

    handleChange(event) {
        this.setState({value: event.target.checked, initialized: true});
        this.props.save(event.target.checked);
    }

    render() {
        const {className} = this.props;
        let value;
        if (this.state.initialized) {
            value = this.state.value;
        }
        else {
            value = (this.props.value) ? this.props.value : false;
            // Don't have text be null or undefined, this will cause React to treat the Input as
            // an uncontrolled component and then later detect that it is now a controlled component.
            // This will log errors in the console
        }

        return (

            <input type="checkbox"
                   className={className}
                   checked={value}
                   onChange={(event) => this.handleChange(event)}
            />
        );
    }
}