import React, {Component} from 'react';

const ENTER_KEY = 13;

export default class TextSaveInput extends Component {

    state = {
        value: null,
        initialized: false,
        saving: false
    };

    componentWillReceiveProps(props) {
        this.setState({value: props.value, saving: false, initialized: true});
    }

    handleChange(event) {
        this.setState({value: event.target.value, initialized: true, saving: false});
    }

    handleKeyDown(event) {
        if (event.keyCode === ENTER_KEY) {
            event.target.blur();
        }
    }

    handleBlur(event) {
        const {value} = (this.state) ? this.state : '';
        if (this.state.value !== this.props.value) {
            this.props.save(value);
            this.setState({...this.state, saving: true});
        }
    }

    render() {
        const {className, placeholder} = this.props;
        let text = (this.state.initialized) ? this.state.value : this.props.value;

        // Don't have text be null or undefined, this will cause React to treat the Input as
        // an uncontrolled component and then later detect that it is now a controlled component.
        // This will log errors in the console
        if (text === null || text === undefined) {
            text = '';
        }

        let icon = (this.state.saving) ? (<span className="glyphicon glyphicon-floppy-save form-control-feedback" aria-hidden="true"/>) : '';

        if (this.props.type === "textArea") {
            return (
                <span>
                <textarea className={className}
                       placeholder={placeholder}
                       value={text}
                       onChange={(event) => this.handleChange(event)}
                       onKeyDown={(event) => this.handleKeyDown(event)}
                       onBlur={(event) => this.handleBlur(event)}
                       rows={this.props.rows}
                />
                    {icon}
            </span>
            );
        }
        else {
            return (
                <span>
                    <input type="text" className={className}
                           placeholder={placeholder}
                           value={text}
                           onChange={(event) => this.handleChange(event)}
                           onKeyDown={(event) => this.handleKeyDown(event)}
                           onBlur={(event) => this.handleBlur(event)}
                    />
                    {icon}
                </span>
            );
        }
    }
}