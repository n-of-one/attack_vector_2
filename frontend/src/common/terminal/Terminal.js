import React, {Component} from 'react';
import TerminalInput from "./TerminalInput";
import TerminalTextLine from "./TerminalTextLine";

class Terminal extends Component {

    state = {};
    dispatch = null;
    bottomRef = React.createRef();
    height = "200px";

    constructor(props) {
        super(props);
        this.state = {...props.terminal};
        this.height = props.height;
        this.dispatch = props.dispatch;
        this.id = props.id;
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
        this.bottomRef.current.scrollIntoView();
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
        if (this.state.readonly) {
            return <div/>
        }
        return (
            <TerminalInput prompt={this.state.prompt} input={this.state.input}/>
        )
        // return (
        //     <div className="terminalLine terminal_input">{this.state.prompt} {this.state.input}<span className="terminalCaret">&nbsp;</span></div>
        // )
    }


    render() {
        return (
            <div className="terminalPanel terminal_scrollbar" style={{height: this.height}} >
                {this.state.lines.map((line, index) => this.renderLine(line, index))}
                {this.renderRenderingLine()}
                {this.renderInput()}
                <div ref={this.bottomRef}/>
            </div>
        );
    }
}

export default Terminal;

// const mapDispatchToProps = (dispatch) => {
//     return {
//         dispatch: dispatch
//     };
// };
//
// let mapStateToProps = (state) => {
//     return {
//         terminal: state.terminal,
//     };
// };
//
// export default connect(mapStateToProps, mapDispatchToProps)(Terminal);
