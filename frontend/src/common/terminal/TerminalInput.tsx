import React from "react"
import {Syntax, SyntaxMap} from "./TerminalReducer"

interface Props {
    prompt: string,
    input: string,
    syntaxHighlighting: SyntaxMap
}

export const TerminalInput = ({input, prompt, syntaxHighlighting}: Props) => {
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
        style = gatherStyles(mapping["rest"])
    }
    else {
        style = gatherStyles(mapping["main"][i])
    }
    return <span className={style} key={i}>{part}</span>
}


const gatherStyles = (styleInput: string) => {
    let styleParts = styleInput.split(" ")
    return styleParts.map(it => "terminal_style_" + it).join(" ")
}
