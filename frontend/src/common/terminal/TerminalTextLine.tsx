import React from "react";
import {TerminalLine} from "./TerminalReducer";

interface Props {
    line: TerminalLine,
    index: string | number,
}

// const renderLine = (line, index) => {
export const TerminalTextLine = ({line, index}: Props) => {
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
}

interface Block {
    text: string,
    className: string,
    type: string
}

const renderBlock = (block: Block, index: string | number) => {
    if (block.type === "text") {
        return <span className={block.className} key={index}>{block.text}</span>
    } else {
        return <span key={index}>&nbsp;</span>
    }
};

const spaceBlock = {text: "", className: "", type: "space"};
const textBlockTemplate = {text: "", className: "", type: "text"};

const parseLine = (line: TerminalLine): Block[] => {
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
