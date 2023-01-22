import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {NodesPanel} from "./left/NodesPanel";
import EditCanvasPanel from "./middle/EditCanvasPanel";
import DetailPanel from "./right/DetailPanel";
import {EditorState} from "../../EditorRootReducer";

export const EditorMain = () =>{

        const dragAndDropState = useSelector((state: EditorState) => state.dragAndDrop );
        const dispatch = useDispatch();

        return (
        <>
            <div className="row editorRow">
                <NodesPanel />
                <EditCanvasPanel dragAndDropState={dragAndDropState} dispatch={dispatch} />
                <DetailPanel/>
            </div>
        </>
        );
    }
