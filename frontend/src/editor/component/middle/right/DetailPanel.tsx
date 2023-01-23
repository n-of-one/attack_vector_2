import React from 'react'
import {ActionsPanel} from "./ActionsPanel"
import {NodeDetailsPanel} from "./layer/NodeDetailsPanel"
import {LayersPanel} from "./LayersPanel"

export const DetailPanel = () => {
    return (
        <>
            <div className="col-lg-5" id="node-library">
                <ActionsPanel/>
                <div className="row">&nbsp;</div>
                <LayersPanel/>
                <div className="row">&nbsp;</div>
                <NodeDetailsPanel/>
            </div>
        </>
    )
}
