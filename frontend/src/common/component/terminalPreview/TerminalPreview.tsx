import {parseTextToTerminalLine} from "../../terminal/TerminalLineParser";
import {TerminalLineData} from "../../terminal/TerminalReducer";
import {TerminalLine} from "../../terminal/TerminalLine";
import React from "react";
import {useSelector} from "react-redux";


interface Props {
    width: string
    prefix?: string
}

interface TerminalPreviewState {
    terminalPreview: string | undefined
}

export const TerminalPreview = (props: Props) => {
    const text = useSelector((state: TerminalPreviewState) => state.terminalPreview)
    const terminalText = (props.prefix ? props.prefix : "") + text
    return <div className="row">
        <div className="col-12">
            <TerminalPreviewContent text={terminalText} width={props.width}/>
        </div>
    </div>
}


const TerminalPreviewContent = ({text, width}: { text: string, width: string }) => {
    const lines = text.split("\n")
    const terminalLines = lines.map((line: string) => parseTextToTerminalLine(line))
    return <div>
        <div className="layerLabel" style={{textAlign: "left", paddingTop: 0}}>Preview</div>
        <div className="terminalPreview" style={{width: width}}>
            <div className="terminalPanel" style={{width: width}}>
                {terminalLines.map((line: TerminalLineData) => <TerminalLine line={line} key={line.key}/>)}
            </div>
        </div>
    </div>
}
