import React from "react";

const HIGHLIGHT_MAP = {
    "autoscan": {
        main: [ "u"],
        rest: "error"
    },
    "scan": {
        main: [ "u", "ok"],
        rest: "error"
    },
    "hack": {
        main: [ "u", "primary"],
        rest: "error"
    },
    "passcode": {
        main: [ "u", "primary", "warn"],
        rest: "error"
    },
};

const renderLine = (input) => {
    const parts = input.split(" ");

    const mapping=HIGHLIGHT_MAP[parts[0]];
    if (!mapping) {
        return <span key="1">{input}</span>;
    }


    const blocks = parts.map( (part, i) => renderPart(part, i, mapping));
    const renderBlocks = [];
    for (let i = 0; i < blocks.length; i++) {
        renderBlocks.push(blocks[i]);
        if (i < blocks.length -1) {
            renderBlocks.push(<span>&nbsp;</span>);
        }
    }


    // if (input.endsWith(" ")){
    //     blocks.push(<span>&nbsp;</span>)
    // }
    return renderBlocks


};

const renderPart = (part, i, mapping) => {
    let style;
    if (i >= mapping["main"].length) {
        style = "terminal_style_" + mapping["rest"] ;
    }
    else {
        style = "terminal_style_" + mapping["main"][i] ;
    }
    return <span className={style} key={i}>{part}</span>
};

export default ({input, prompt}) => {
    // return (
    //     <div className="terminalLine terminal_input">{prompt} {input}<span className="terminalCaret">&nbsp;</span></div>
    // )
    return (
        <div className="terminalLine terminal_input">
            <span>{prompt}</span>
            {renderLine(input)}
            <span className="terminalCaret">&nbsp;</span>
        </div>
    )
}