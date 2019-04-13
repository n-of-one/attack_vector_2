import React, {Component} from 'react';
import { connect } from 'react-redux';
import {TERMINAL_KEY_PRESS, TERMINAL_TICK} from "./TerminalActions";

let terminalIntervalId = null;

class Terminal extends Component {

    state = {};
    dispatch = null;

    constructor(props) {
        super(props);
        this.state = { ...props.terminal };
        this.dispatch = props.dispatch;

        if (!terminalIntervalId) {
            terminalIntervalId = setInterval(() => {
                this.dispatch({type: TERMINAL_TICK});
            }, 100)
        }
    }

    componentWillReceiveProps(props) {
        this.setState({ ...props.terminal });
    }

    handleKeyDown(event) {
        let {keyCode, key} = event;
        event.preventDefault();
        this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode});
    }

    renderLine(line, index) {
        return (<div className="terminalLine" key={index} >{line.data}&nbsp;</div>)
    }
    renderInput() {
        if (this.readonly) {
            return <div />
        }
        return (
            <div className="terminalLine">{this.state.prompt} {this.state.input}<span className="terminalCaret">&nbsp;</span></div>
        )
    }


    render() {
        let lines = this.state.lines;
        return (
            <div className="terminalPanel" onKeyDown={event => this.handleKeyDown(event)} tabIndex="0">
                {lines.map((line, index) => this.renderLine(line, index))}
                {this.renderInput()}
        </div>
        );
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    };
};

let mapStateToProps = (state) => {
    return {
        terminal: state.terminal,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(Terminal);
