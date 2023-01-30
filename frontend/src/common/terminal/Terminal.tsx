import React, {Component, createRef} from 'react';
import {TerminalInput} from "./TerminalInput";
import {TerminalTextLine} from "./TerminalTextLine";
import {terminalManager} from "./TerminalManager";
import {TERMINAL_SUBMIT, TerminalState} from "./TerminalReducer";
import {Dispatch} from "redux";

interface Props {
    terminal: TerminalState,
    height?: number | string
    submit?: (input: string) => void
    dispatch?: Dispatch | null
    className?: string
}

export class Terminal extends Component<Props> {

    state: TerminalState
    bottomRef = createRef<HTMLDivElement>()
    height: number | string = 200

    submitMethod: (input: string) => void
    dispatch: Dispatch | null = null

    constructor(props: Props) {
        super(props);
        this.state = {...props.terminal};
        if (props.height) this.height = props.height;
        terminalManager.registerTerminal(this.state.id, this);
        this.submitMethod = (props.submit) ? props.submit : (input: string) => console.log("Ingored terminal input: " + input)
        if (props.dispatch) this.dispatch = props.dispatch;
    }

    submit(key: string) {
        this.submitMethod(this.state.input);
        this.dispatch!({type: TERMINAL_SUBMIT, key: key, command: this.state.input, terminalId: this.state.id});
    }

    UNSAFE_componentWillReceiveProps(props: Props) {
        this.setState({...props.terminal});
    }

    componentDidMount() {
        this.scrollToBottom();
    }

    componentDidUpdate() {
        this.scrollToBottom();
    }

    scrollToBottom() {
        if (this.state.autoScroll && this.bottomRef.current != null) {
            this.bottomRef.current.scrollIntoView();
        }
    }


    /** The line that is being displayed one character at a time */
    renderRenderingLine() {
        if (!this.state.renderingLine) {
            return <div/>
        }
        return <TerminalTextLine line={this.state.renderingLine} />
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
                {this.state.lines.map((line, index) => <TerminalTextLine line={line} key={index}/>)}
                {this.renderRenderingLine()}
                {this.renderInput()}
                <div ref={this.bottomRef}/>
            </div>;
        } else {
            return <div className="terminalPanel" style={{height: this.height}}>
                {this.renderInput()}
            </div>;
        }
    }
}
