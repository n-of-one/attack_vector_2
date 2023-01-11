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
            <div className="row">
                <div className="col-lg-6" style={{fontSize: "12px"}}>
                    <input type="text" className="form-control"
                           placeholder={placeholder}
                           value={text}
                           onChange={(event) => this.handleChange(event)}
                           onKeyDown={(event) => this.handleKeyDown(event)}
                    />
                </div>
                <div className="col-lg-6">
                    <button type="button" className={totalButtonClass} style={{fontSize: "12px"}} onClick={() => this.save()}>{buttonLabel}</button>
                </div>
            </div>
        );
    }
}