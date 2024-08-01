import React from "react"
import {Syntax, SyntaxMap, TerminalState} from "./TerminalReducer"

interface Props {
    terminalState: TerminalState
}

export const TerminalInput = ({terminalState}: Props) => {
    if (terminalState.readOnly || (terminalState.blockedWhileRendering && (terminalState.renderingLine || terminalState.unrenderedLines.length > 0))) {
        return (
            <div className="terminalLine terminal_input">
                <span>&nbsp;</span>
            </div>
        )
    }

    const {input, prompt, syntaxHighlighting} = terminalState
    // return (
    //     <div className="terminalLine terminal_input">{prompt} {input}<span className="terminalCaret">&nbsp;</span></div>
    // )
    return (
        <div className="terminalLine terminal_input">
            <span>{prompt}</span>
            {renderLine(input, syntaxHighlighting)}
            <span className="terminalCaret">&nbsp;</span>
        </div>
    )
}


const renderLine = (input: string, syntaxHighlighting: SyntaxMap) => {
    const parts = input.split(" ")

    let mapping = syntaxHighlighting[parts[0]]

    if (!mapping) {
        if (!mapping) {
            return <span key="1">{input}</span>
        }
    }

    const blocks = parts.map((part, i) => renderPart(part, i, mapping))
    const renderBlocks = []
    for (let i = 0; i < blocks.length; i++) {
        renderBlocks.push(blocks[i])
        if (i < blocks.length - 1) {
            renderBlocks.push(<span key={"space" + i}>&nbsp;</span>)
        }
    }


    // if (input.endsWith(" ")){
    //     blocks.push(<span>&nbsp;</span>)
    // }
    return renderBlocks
}


const renderPart = (part: string, i: number, mapping: Syntax) => {
    let style
    if (i >= mapping["main"].length) {
        style = mapping["rest"]
    }
    else {
        style = mapping["main"][i]
    }
    return <span className={style} key={i}>{part}</span>
}
