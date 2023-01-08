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
            this.props.save(this.state.value);
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
        const text = this.state.value ? this.state.value : '';

        return (
            <div className="form-inline">
                <div className="form-group">
                    <input type="text" className="form-control" style={{fontSize: "12px"}}
                           placeholder={placeholder}
                           value={text}
                           onChange={(event) => this.handleChange(event)}
                           onKeyDown={(event) => this.handleKeyDown(event)}
                    />
                </div>&nbsp;
                <button type="button" className={totalButtonClass} style={{fontSize: "12px"}} onClick={() => this.save()}>{buttonLabel}</button>
            </div>
        );
    }
}