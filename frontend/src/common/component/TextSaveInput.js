import React, {Component} from 'react';

const ENTER_KEY = 13;

/**
 * Props:
 * id: "site_name"
 * className: "form-control"
 * placeholder: "Display name"
 * value: "mySite"
 * save: (value) => {...}
 * type: "textArea" | * (default)
 */
export default class TextSaveInput extends Component {

    constructor(props, context, updater){
        super(props, context, updater);
        this.state = {
            value: props.value,
            saving: false,
            initialized: true};
    }

    componentWillReceiveProps = (props) => {
        this.handleBlur(); // in case of select new node while current node still has pending changes.
        this.setState({value: props.value, saving: false, initialized: true});
    };

    componentWillUnmount = () => {
        this.handleBlur(); // in case of deselecting node with pending data.
    };

    handleChange = (event) => {
        const newValue = event.target.value;
        this.setState({value: newValue, initialized: true, saving: false});
    };

    handleKeyDown = (event) => {
        if (event.keyCode === ENTER_KEY) {
            event.target.blur();
        }
    };

    handleBlur = (event) => {
        if (!this.state.initialized) {
            return;
        }
        if (this.state.value !== this.props.value) {
            this.props.save(this.state.value);
            this.setState({...this.state, saving: true});
        }
    };

    render() {
        const {className, placeholder} = this.props;
        let text = this.state.value;

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