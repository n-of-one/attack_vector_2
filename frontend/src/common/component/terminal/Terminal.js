import React, {Component} from 'react';

const ENTER_KEY = 13;
const BACKSPACE = 8;
const TAB = 9;

export default class Terminal extends Component {

    state = {};

    constructor(props) {
        super(props);
        this.state = {
            value: "",
            lines: props.terminal.lines,
            prompt: props.terminal.prompt,
            readonly: props.terminal.readonly,
        }
    }

    componentWillReceiveProps(props) {
        let {lines, prompt, readonly} = props.terminal;
        this.setState({
            lines: lines,
            prompt: prompt,
            readonly: readonly
        });
    }

    handleKeyDown(event) {
        let {keyCode, key} = event;
        if (keyCode === ENTER_KEY) {
            let line = this.state.value;
            let lines = [ ...this.state.lines, {type: "text", data: line} ];
            this.setState({
                lines: lines,
                value: "",
            });
            // this.props.submit(this.state.value);
            return
        }
        let value = this.state.value;
        if (keyCode === BACKSPACE && value.length > 0) {
            let newValue = value.substr(0, value.length-1);
            this.setState({value: newValue});
            return;
        }
        if (keyCode === TAB ) {
            this.setState({value: this.state.value + "[t]"});
            event.preventDefault();
            return;
        }
        if (key.length === 1) {
            this.setState({value: this.state.value + event.key});
        }
        else {
            // ignore other keys for now.
            // this.setState({value: this.state.value + "[" + keyCode + "] "});
        }

    }

    submit() {
        this.props.save(this.state.value);
        this.setState({value: ""});
    }

    renderLine(line) {
        return (<div className="terminalLine" >{line.data}&nbsp;</div>)
    }

    renderInput() {
        return (
            <div className="terminalLine">{prompt} {this.state.value}<span className="terminalCaret">&nbsp;</span></div>
        )
    }


    render() {
        let lines = this.state.lines;
        return (
            <div className="terminalPanel" onKeyDown={event => this.handleKeyDown(event)} tabIndex="0">
                {lines.map((line) => this.renderLine(line))}
                {this.renderInput()}
        </div>
        );
    }
}
