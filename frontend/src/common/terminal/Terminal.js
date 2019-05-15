import React, {Component} from 'react';

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
        let lineClasses = (line.class) ? line.class : [];
        let classNames = "terminalLine";
        lineClasses.forEach(name => {
            classNames += " terminal_" + name
        });

        let blocks = this.parseLine(line);
        return (
            <div className={classNames} key={index}>{blocks.map((block, index) => this.renderBlock(block, index))}
                &nbsp;
            </div>)
    }

    renderBlock(block, index) {
        if (block.type === "text") {
            return <span className={block.className} key={index}>{block.text}</span>
        }
        else {
            return <span key={index}>&nbsp;</span>
        }
    }

    parseLine(line) {
        let blocks = [];

        let mode = "text";
        const spaceBlock = {text: "", className: "", type: "space"};
        const textBlockTemplate = {text: "", className: "", type: "text"};
        let block = {...textBlockTemplate};
        let currentClassName = "";
        for (let i = 0; i < line.data.length; i++) {
            let c = line.data.charAt(i);

            if (c === "[") {
                if (mode === "text") {
                    mode = "style";
                    if (block.text) {
                        block.className = currentClassName;
                        blocks.push(block);
                    }
                    block = {...textBlockTemplate};
                    continue;
                }
                else {
                    // escaped '[' character
                    mode = "text";
                    block.text += c;
                    continue;
                }
            }
            if (c === " " && mode === "text") {
                if (block.text) {
                    block.className = currentClassName;
                    blocks.push(block);
                }
                blocks.push(spaceBlock);
                block = {...textBlockTemplate};
                continue;

            }
            if (c === "]" && mode === "style") {
                mode = "text";
                let style = block.text === "/" ? "" : block.text;
                let styleParts = style.split(" ");
                currentClassName = styleParts.map(it => "terminal_style_" + it).join(" ");
                block.text = "";
                continue;
            }

            block.text += c;
        }
        if (mode === "text" && block.text) {
            block.className = currentClassName;
            blocks.push(block);
        }

        return blocks;
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
            <div className="terminalLine terminal_input">{this.state.prompt} {this.state.input}<span className="terminalCaret">&nbsp;</span></div>
        )
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
