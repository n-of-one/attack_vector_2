import React, {createRef} from 'react';
import {TerminalInput} from "./TerminalInput";
import {TerminalTextLine} from "./TerminalTextLine";
import {terminalManager} from "./TerminalManager";
import {TerminalState} from "./TerminalReducer";

/// The line that is being displayed one character at a time
const renderRenderingLine = (terminalState: TerminalState) => {
    if (!terminalState.renderingLine) {
        return <div/>
    }
    return <TerminalTextLine line={terminalState.renderingLine}/>
}

const renderInput = (terminalState: TerminalState) => {
    if (terminalState.readOnly) {
        return <div/>
    }
    return (
        <TerminalInput prompt={terminalState.prompt} input={terminalState.input} syntaxHighlighting={terminalState.syntaxHighlighting}/>
    )
}

interface Props {
    terminalState: TerminalState,
    submit?: () => void
    height?: number | string,
}


export const Terminal = ({terminalState, height, submit}: Props) => {

    const bottomRef = createRef<HTMLDivElement>()
    React.useEffect(() => {
        if (terminalState.autoScroll && bottomRef.current !== null) {
            bottomRef.current.scrollIntoView()
        }
    })

    if (submit) {
        terminalManager.registerTerminalSubmit(terminalState.id, submit);
    }

    if (terminalState.renderOutput) {
        return <div className="terminalPanel terminal_scrollbar" style={{height: height}}>
            {terminalState.lines.map((line, index) => <TerminalTextLine line={line} key={index}/>)}
            {renderRenderingLine(terminalState)}
            {renderInput(terminalState)}
            <div ref={bottomRef}/>
        </div>;
    } else {
        return <div className="terminalPanel" style={{height: height}}>
            {renderInput(terminalState)}
        </div>;
    }

}
