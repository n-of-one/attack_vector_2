import React from 'react'
import {TerminalPreview} from "../../../../../../common/component/terminalPreview/TerminalPreview";

interface Props {
    width: string
    prefix?: string
}

export const AttributeTerminalPreview = (props: Props) => {
    return <div className="row">
        <div className="col-12">
            <TerminalPreview prefix={props.prefix} />
        </div>
    </div>
}
