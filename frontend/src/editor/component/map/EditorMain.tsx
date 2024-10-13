import React from 'react'
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
