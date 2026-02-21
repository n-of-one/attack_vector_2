import React from 'react'
import {useSelector} from "react-redux";
import {EditorTextState} from "../EditorTextRootReducer";
import {TextSaveInput, TextSaveType} from "../../common/component/TextSaveInput";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {TerminalPreview} from "../../common/component/terminalPreview/TerminalPreview";
/* eslint react/jsx-no-target-blank  : 0*/

export const EditLayerHome = () => {
    const state = useSelector((state: EditorTextState) => state)


    const save = (value: string) => {
        if (state.type === "LAYER") {
            const payload = {siteId: state.siteId, nodeId: state.nodeId, layerId: state.layerId, key: state.key, value}
            webSocketConnection.send("/editor/editLayerData", payload)
        }
        if (state.type === "EFFECT") {
            const payload = {scriptTypeId: state.scriptTypeId, effectNumber: state.effectNumber, value}
            webSocketConnection.send("/gm/scriptType/editEffect", payload)

        }
    }


    return (
        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-12 text">
                    <h1 className="text_gold">Text of {state.header}</h1>
                </div>
            </div>
            <hr/>
            <div className="row">
                <div className="col-lg-2">
                </div>
                <div className="col-lg-6">
                    <TextSaveInput className="form-control input-sm" placeholder="" value={state.initialValue} id="1" type={TextSaveType.TEXTAREA}
                                   save={value => save(value)} sendEvent={true} rows={10}/>
                </div>
                <div className="col-lg-1">
                </div>
                <div className="col-lg-3">
                    <div className="text">
                        Formatting options:<br/>
                        <br/>
                        [primary]<span className="t_primary">blue</span>[/]<br/>
                        [pri]<span className="t_pri">blue</span>[/]<br/>
                        [ok]<span className="t_ok">green</span>[/]<br/>
                        [info]<span className="t_info">light blue</span>[/]<br/>
                        [warn]<span className="t_warn">orange</span>[/]<br/>
                        [error]<span className="t_error">red</span>[/]<br/>
                        [mute]<span className="t_mute">dark gray</span>[/]<br/>
                        [b]<span className="t_b">bold</span>[/]<br/>
                        [i]<span className="t_i">italics</span>[/]<br/>
                        [s]<span className="t_s">strikethrough</span>[/]<br/>
                        [u]<span className="t_u">underline</span>[/]<br/>
                        [us]<span className="t_us">underline strikethrough</span>[/]<br/>
                        <br/>
                        [https://example.com]<a href="https://example.com" target="_blank">link</a>[/]<br/>
                        !https://example.com/image! -&gt; (image)<br/>
                        [https://example.com]!https://example.com/image![/] -&gt; (<a href="https://example.com" target="_blank">image</a>)<br/>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-lg-2">
                </div>
                <div className="col-lg-6">
                    <TerminalPreview />
                </div>
            </div>
        </div>
    )
}
