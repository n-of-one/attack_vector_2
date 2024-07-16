import React from "react"
import {TerminalBlockType, TerminalLineData, TerminalLineBlock} from "./TerminalReducer"


interface Props {
    line: TerminalLineData,
    lastBlockIndex?: number,
    characterIndex?: number,
}


// const renderLine = (line, index) => {
export const TerminalLine = ({line, lastBlockIndex, characterIndex}: Props) => {

    const blocksToRender = determineBlocksToRender(line, lastBlockIndex, characterIndex)

    return (
        <div className="terminalLine">
            {
                blocksToRender.map((block: TerminalLineBlock) => <Block block={block}/>)
            }
            &nbsp;
        </div>)
}

const determineBlocksToRender = (line: TerminalLineData, lastBlockIndex: number | undefined, characterIndex: number | undefined) => {
    if (lastBlockIndex === undefined ) {
        return line.blocks
    }

    const fullyRenderedBlocks = line.blocks.slice(0, lastBlockIndex)
    const currentlyRenderingBlock = line.blocks[lastBlockIndex]
    const blockToRender = {...currentlyRenderingBlock, text: currentlyRenderingBlock.text.slice(0, characterIndex)}

    return [...fullyRenderedBlocks, blockToRender]
}



interface BlockProps {
    block: TerminalLineBlock
}

const Block = ({block}: BlockProps) => {
    switch (block.type) {
        case TerminalBlockType.TEXT:
            return <span className={block.className} key={block.key}>{block.text}</span>
        case TerminalBlockType.SPACE:
            return <span  key={block.key}>&nbsp;</span>
        case TerminalBlockType.LINK:
            return <a href={block.link} className={block.className} target={`${block.key}`}   key={block.key}>{block.text}</a>
        case TerminalBlockType.EMPTY_LINE:
            return <span  key={block.key}></span>
        default:
            return <span className={block.className}  key={block.key}>(unknown block type: {block.type}: {block.text}</span>

    }
}
