import React from 'react'
import {
    TYPE_DATA_STORE, TYPE_MANUAL_1, TYPE_MANUAL_2, TYPE_MANUAL_3, TYPE_PASSCODE_STORE, TYPE_RESOURCE_STORE, TYPE_SYSCON,
    TYPE_ICE_1, TYPE_ICE_2, TYPE_ICE_3, TYPE_TRANSIT_1, TYPE_TRANSIT_2, TYPE_TRANSIT_3, TYPE_TRANSIT_4, TYPE_UNHACKABLE
} from "../../../../common/enums/NodeTypesNames"
import {EditorNodeImage} from "./EditorNodeImage"
import {editorCanvas} from "../middle/EditorCanvas"

/* eslint jsx-a11y/alt-text: 0*/

export const NodesPanel = () => {
    const onLoad = () => {
        editorCanvas.render()
    }

    return (
        <div className="col-lg-2" id="node-library">
            <div className="row">
                <div className="col-lg-12 darkWell node_panel_no_right_padding" id="node-library">
                    <br/>
                    <p className="text-muted">Content nodes</p>
                    <EditorNodeImage type={TYPE_SYSCON} ice={false} title="Syscon" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_DATA_STORE} ice={false} title="Data (info) store" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_PASSCODE_STORE} ice={false} title="Passcode store" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_RESOURCE_STORE} ice={false} title="Resource store" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_TRANSIT_1} ice={false} title="Transit" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_TRANSIT_2} ice={false} title="Transit" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_TRANSIT_3} ice={false} title="Transit" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_TRANSIT_4} ice={false} title="Transit" onLoad={onLoad}/>
                    <p className="text-muted">Automated mini game ice nodes</p>
                    <EditorNodeImage type={TYPE_ICE_1} ice={true} title="Ice: word search" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_ICE_2} ice={true} title="Ice: magic eye" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_ICE_3} ice={true} title="Ice: password guess" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_UNHACKABLE} ice={true} title="Ice: unhackable" onLoad={onLoad}/>
                    <p className="text-muted">Manual mini game ice nodes</p>
                    <EditorNodeImage type={TYPE_MANUAL_1} ice={true} title="Ice: manual / gm puzzle" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_MANUAL_2} ice={true} title="Ice: manual / gm puzzle" onLoad={onLoad}/>
                    <EditorNodeImage type={TYPE_MANUAL_3} ice={true} title="Ice: manual / gm puzzle" onLoad={onLoad}/>
                    <br/>
                    <br/>
                </div>
            </div>
            <div className="row">
                <div className="col-lg-12">
                    <span className="text-muted"> Nodes, drag to editor</span>
                </div>
            </div>
        </div>
    )
}
