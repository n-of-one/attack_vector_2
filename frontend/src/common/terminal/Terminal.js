import React, {Component} from 'react';
import TerminalInput from "./TerminalInput";
import TerminalTextLine from "./TerminalTextLine";
import terminalManager from "./TerminalManager";
import {TERMINAL_SUBMIT} from "./TerminalActions";

class Terminal extends Component {

    state = {};
    bottomRef = React.createRef();
    height = "200px";
    submitMethod = (input) => {
        console.log("Ingored terminal input: " + input);
    };
    dispatch = null;

    constructor(props) {
        super(props);
        this.state = {...props.terminal};
        this.height = props.height;
        terminalManager.registerTerminal(this.state.id, this);
        if (props.submit) {
            this.submitMethod = props.submit;
        }
        this.dispatch = props.dispatch;
    }

    submit(key) {
        this.submitMethod(this.state.input);
        this.dispatch({type: TERMINAL_SUBMIT, key: key, command: this.state.input, terminalId: this.state.id});
    }

    componentWillReceiveProps(props) {
        this.setState({...props.terminal});
    }

    componentDidMount() {
        this.scrollToBottom();
    }

    componentDidUpdate() {
        this.scrollToBottom();
    }

    scrollToBottom() {
        if (this.state.autoScroll && this.state.active) {
            this.bottomRef.current.scrollIntoView();
        }
    }

    renderLine(line, index) {
        return <TerminalTextLine line={line} index={index} key={index}/>
    }

    /** The line that is being displayed one character at a time */
    renderRenderingLine() {
        if (!this.state.renderingLine) {
            return <div/>
        }
        return this.renderLine(this.state.renderingLine, "renderingLine");
    }

    renderInput() {
        if (this.state.readOnly) {
            return <div/>
        }
        return (
            <TerminalInput prompt={this.state.prompt} input={this.state.input} syntaxHighlighting={this.state.syntaxHighlighting}/>
        )
    }

    render() {
        if (this.state.renderOutput) {
            return <div className="terminalPanel terminal_scrollbar" style={{height: this.height}}>
                {this.state.lines.map((line, index) => this.renderLine(line, index))}
                {this.renderRenderingLine()}
                {this.renderInput()}
                <div ref={this.bottomRef}/>
            </div>;
        }
        else {
            return <div className="terminalPanel" style={{height: this.height}}>
                {this.renderInput()}
            </div>;
        }
    }
}

export default Terminal;
