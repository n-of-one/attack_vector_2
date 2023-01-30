import React from "react";
import {TerminalLine} from "./TerminalReducer";

interface BlockI {
    text: string,
    className: string,
    type: string
}

const Block = (block: BlockI) => {
    if (block.type === "text") {
        return <span className={block.className}>{block.text}</span>
    } else {
        return <span>&nbsp;</span>
    }
};

interface Props {
    line: TerminalLine,
}

// const renderLine = (line, index) => {
export const TerminalTextLine = ({line}: Props) => {
    let lineClasses = (line.class) ? line.class : [];
    let classNames = "terminalLine";
    lineClasses.forEach(name => {
        classNames += " terminal_" + name
    });

    let blocks = parseLine(line);
    return (
        <div className={classNames}>
            {
                blocks.map((block, index) => <Block key={index} text={block.text} className={block.className} type={block.type}/>)
            }
            &nbsp;
        </div>)
}


const spaceBlock = {text: "", className: "", type: "space"};
const textBlockTemplate = {text: "", className: "", type: "text"};

const parseLine = (line: TerminalLine): BlockI[] => {
    let blocks = [];
    let mode = "text";
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
            } else {
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
