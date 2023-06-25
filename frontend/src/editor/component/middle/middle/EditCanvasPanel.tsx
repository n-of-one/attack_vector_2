import React, {useEffect} from 'react'
import {editorCanvas} from "./EditorCanvas"
import {useDispatch, useSelector} from "react-redux"
import {EditorState} from "../../../EditorRootReducer"
import {delay} from "../../../../common/util/Util"
import {sendAddNode} from "../../../server/EditorServerClient"

/* eslint react-hooks/exhaustive-deps: 0*/

export const EditCanvasPanel = () => {

    const dragAndDropState = useSelector((state: EditorState) => state.dragAndDrop)
    const dispatch = useDispatch()

    useEffect(() => {
        delay( () => editorCanvas.init(dispatch))
    },[])

    const allowDrop = (event: any) => {
        event.preventDefault()
    }


    const drop_image_and_create_node = (syntheticEvent: any) => {
        let event = syntheticEvent.nativeEvent
        const x = event.offsetX - dragAndDropState.dx
        const y = event.offsetY - dragAndDropState.dy
        const type = dragAndDropState.type.toUpperCase()

        sendAddNode({x, y, type})
        event.preventDefault()
    }

    return (
        <div className="col-lg-5" id="canvas-col">
            <div id="canvas-container" onDragOver={(event) => allowDrop(event)}
                 onDrop={(event) => drop_image_and_create_node(event)}>
                <canvas id="canvas" width="607" height="715" style={{"borderRadius": "3px 3px 3px 3px"}}/>
            </div>
        </div>
    )
}
