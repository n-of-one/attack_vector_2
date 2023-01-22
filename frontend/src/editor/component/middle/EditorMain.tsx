import React from 'react';
import {NodesPanel} from "./left/NodesPanel";
import {EditCanvasPanel} from "./middle/EditCanvasPanel";
import {DetailPanel} from "./right/DetailPanel";

export const EditorMain = () =>{


        return (
        <>
            <div className="row editorRow">
                <NodesPanel />
                <EditCanvasPanel />
                <DetailPanel/>
            </div>
        </>
        );
    }
