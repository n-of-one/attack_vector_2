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
 *
 * When the focus is lost or enter is hit, the component fires its "save" method as provided by the props.
 *
 * During the saving, a floppy disk icon is shown.
 */

/*
This Component is almost a "fully uncontrolled component with a key"
as described in: https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html

However, it still needs to know when the save has finished, so that it can remove the floppy disk icon.
For this, the componentWillReceiveProps method is used.

The key needs to be set at a higher level, where we have context to base this key on.
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

        let icon = (this.state.saving) ? (<span className="form-control-feedback"><span className="glyphicon glyphicon-floppy-save" aria-hidden="true"/></span>) : ''; ;

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