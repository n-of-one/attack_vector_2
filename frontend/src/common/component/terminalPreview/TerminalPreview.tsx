import {parseTextToTerminalLine} from "../../terminal/TerminalLineParser";
import {TerminalLineData} from "../../terminal/TerminalReducer";
import {TerminalLine} from "../../terminal/TerminalLine";
import React from "react";
import {useSelector} from "react-redux";


interface Props {
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
            <TerminalPreviewContent text={terminalText}/>
        </div>
    </div>
}


const TerminalPreviewContent = ({text}: { text: string }) => {
    const lines = text.split("\n")
    const terminalLines = lines.map((line: string) => parseTextToTerminalLine(line))
    return <div>
        <div className="layerLabel" style={{textAlign: "left", paddingTop: 0}}>Preview</div>
        <div className="terminalPreview" >
            <div className="terminalPanel" style={{paddingLeft: "0px", paddingRight: "0px", width: "616px", fontSize: "14px" }}>
                {terminalLines.map((line: TerminalLineData) => <TerminalLine line={line} key={line.key}/>)}
            </div>
        </div>
    </div>
}
