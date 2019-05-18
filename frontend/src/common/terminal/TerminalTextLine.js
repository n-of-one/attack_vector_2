import React from "react";


// const renderLine = (line, index) => {
export default ({line, index}) => {
    let lineClasses = (line.class) ? line.class : [];
    let classNames = "terminalLine";
    lineClasses.forEach(name => {
        classNames += " terminal_" + name
    });

    let blocks = parseLine(line);
    return (
        <div className={classNames} key={index}>{blocks.map((block, index) => renderBlock(block, index))}
            &nbsp;
        </div>)
};

const renderBlock = (block, index) => {
    if (block.type === "text") {
        return <span className={block.className} key={index}>{block.text}</span>
    }
    else {
        return <span key={index}>&nbsp;</span>
    }
};

const parseLine = (line) => {
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
