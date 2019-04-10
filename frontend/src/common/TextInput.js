import React, {Component} from 'react';

const ENTER_KEY = 13;

export default class TextSaveInput extends Component {

    state = {
        value: "",
    };

    componentWillReceiveProps(props) {
        this.setState({value: props.value});
    }

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleKeyDown(event) {
        if (event.keyCode === ENTER_KEY) {
            this.props.save();
        }
    }

    save() {
        this.props.save(this.state.value);
        if (this.props.clearAfterSubmit) {
            this.setState({value: ""});
        }
    }


    render() {
        const {placeholder, buttonLabel, buttonClass} = this.props;

        const totalButtonClass = "btn " + buttonClass;
        let text = this.state.value;
        return (
            <div className="form-inline">
                <div className="form-group">
                    <input type="text" className="form-control"
                           placeholder={placeholder}
                           value={text}
                           onChange={(event) => this.handleChange(event)}
                           onKeyDown={(event) => this.handleKeyDown(event)}
                    />
                </div>
                <button type="button" className={totalButtonClass} onClick={() => this.save()}>{buttonLabel}</button>
            </div>
        );
    }
}