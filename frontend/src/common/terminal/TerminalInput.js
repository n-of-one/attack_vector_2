import React from "react";

const renderLine = (input, syntaxHighlighting) => {
    const parts = input.split(" ");

    let mapping = syntaxHighlighting[parts[0]];

    if (!mapping) {
        if (!mapping) {
            return <span key="1">{input}</span>;
        }
    }

    const blocks = parts.map((part, i) => renderPart(part, i, mapping));
    const renderBlocks = [];
    for (let i = 0; i < blocks.length; i++) {
        renderBlocks.push(blocks[i]);
        if (i < blocks.length - 1) {
            renderBlocks.push(<span key={"space" + i}>&nbsp;</span>);
        }
    }


    // if (input.endsWith(" ")){
    //     blocks.push(<span>&nbsp;</span>)
    // }
    return renderBlocks


};

const gatherStyles = (styleInput) => {
    let styleParts = styleInput.split(" ");
    return styleParts.map(it => "terminal_style_" + it).join(" ");
};

const renderPart = (part, i, mapping) => {
    let style;
    if (i >= mapping["main"].length) {
        style = gatherStyles(mapping["rest"]);
    }
    else {
        style = gatherStyles(mapping["main"][i]);
    }
    return <span className={style} key={i}>{part}</span>
};

export default ({input, prompt, syntaxHighlighting}) => {
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