import React from 'react';
import {
    DATA_STORE, MANUAL_1, MANUAL_2, MANUAL_3, PASSCODE_STORE, RESOURCE_STORE, SYSCON,
    ICE_1, ICE_2, ICE_3, TRANSIT_1, TRANSIT_2, TRANSIT_3, TRANSIT_4, UNHACKABLE
} from "../../../../common/enums/NodeTypesNames";
import {EditorNodeImage} from "./EditorNodeImage";
import editorCanvas from "../middle/EditorCanvas";

/* eslint jsx-a11y/alt-text: 0*/

export const NodesPanel = () => {
    const onLoad = () => {
        editorCanvas.render();
    }

    return (
        <div className="col-lg-2" id="node-library">
            <div className="row">
                <div className="col-lg-12 darkWell node_panel_no_right_padding" id="node-library">
                    <br/>
                    <p className="text-muted">Content nodes</p>
                    <EditorNodeImage type={SYSCON} ice={false} title="Syscon" onLoad={onLoad}/>
                    <EditorNodeImage type={DATA_STORE} ice={false} title="Data (info) store" onLoad={onLoad}/>
                    <EditorNodeImage type={PASSCODE_STORE} ice={false} title="Passcode store" onLoad={onLoad}/>
                    <EditorNodeImage type={RESOURCE_STORE} ice={false} title="Resource store" onLoad={onLoad}/>
                    <EditorNodeImage type={TRANSIT_1} ice={false} title="Transit" onLoad={onLoad}/>
                    <EditorNodeImage type={TRANSIT_2} ice={false} title="Transit" onLoad={onLoad}/>
                    <EditorNodeImage type={TRANSIT_3} ice={false} title="Transit" onLoad={onLoad}/>
                    <EditorNodeImage type={TRANSIT_4} ice={false} title="Transit" onLoad={onLoad}/>
                    <p className="text-muted">Automated mini game ice nodes</p>
                    <EditorNodeImage type={ICE_1} ice={true} title="Ice: word search" onLoad={onLoad}/>
                    <EditorNodeImage type={ICE_2} ice={true} title="Ice: magic eye" onLoad={onLoad}/>
                    <EditorNodeImage type={ICE_3} ice={true} title="Ice: password guess" onLoad={onLoad}/>
                    <EditorNodeImage type={UNHACKABLE} ice={true} title="Ice: unhackable" onLoad={onLoad}/>
                    <p className="text-muted">Manual mini game ice nodes</p>
                    <EditorNodeImage type={MANUAL_1} ice={true} title="Ice: manual / gm puzzle" onLoad={onLoad}/>
                    <EditorNodeImage type={MANUAL_2} ice={true} title="Ice: manual / gm puzzle" onLoad={onLoad}/>
                    <EditorNodeImage type={MANUAL_3} ice={true} title="Ice: manual / gm puzzle" onLoad={onLoad}/>
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
    );
}
