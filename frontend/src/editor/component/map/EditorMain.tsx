import React from 'react'
import {NodesPanel} from "../nodes/NodesPanel"
import {EditCanvasPanel} from "./canvas/EditCanvasPanel"
import {DetailPanel} from "./panel/DetailPanel"

export const EditorMain = () =>{
        return (
    <div className="d-flex flex-row">
        <EditCanvasPanel/>
        <DetailPanel/>
    </div>
)
}
