import React from 'react'
import {ActionsPanel} from "./ActionsPanel"
import {NodeDetailsPanel} from "./layer/NodeDetailsPanel"
import {LayersPanel} from "./LayersPanel"

export const DetailPanel = () => {
    return (
        <div className="p-1" id="node-library" style={{marginLeft: "8px", width: "621px"}}>
            <ActionsPanel/>
            {/*<div className="row">&nbsp;</div>*/}
            <LayersPanel/>
            {/*<div className="row">&nbsp;</div>*/}
            <NodeDetailsPanel/>
        </div>
    )
}
