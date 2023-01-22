import React from 'react';
import EditorCanvas from "./EditorCanvas";
import {useDispatch, useSelector} from "react-redux";
import {EditorState} from "../../../EditorRootReducer";
import {useRunOnceDelayed} from "../../../../common/Util";
import {sendAddNode} from "../../../server/ServerClient";


export const EditCanvasPanel = () => {

    const dragAndDropState = useSelector((state: EditorState) => state.dragAndDrop );
    const siteId = useSelector((state: EditorState) => state.siteData.siteId );
    const dispatch = useDispatch();

    useRunOnceDelayed( () => {
        EditorCanvas.init(dispatch);
    })

    const allowDrop = (event: any) => { event.preventDefault() }


    const drop_image_and_create_node = (syntheticEvent: any) => {

        let event = syntheticEvent.nativeEvent;
        let x = event.offsetX;
        let y = event.offsetY;

        sendAddNode(x, y, dragAndDropState, siteId);
        // dispatch({type: DRAG_DROP_END, x: x, y: y, dragAndDropState: dragAndDropState});
        event.preventDefault();
    }

    return (
        <div className="col-lg-5" id="canvas-col">
            <div id="canvas-container" onDragOver={(event) => allowDrop(event)}
                 onDrop={ (event) => drop_image_and_create_node(event)}>
                <canvas id="canvas" width="607" height="715" style={{"borderRadius": "3px 3px 3px 3px"}}/>
            </div>
        </div>
    );
}
