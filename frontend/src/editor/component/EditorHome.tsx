import React from 'react';
import EditorMain from "./middle/EditorMain";
import {EditorTop} from "./top/EditorTop";
import EditorBottom from "./bottom/EditorBottom";

export const EditorHome = () => {
    return (
        <div className="container-fluid" data-bs-theme="dark">
            <br/>
            <EditorTop/>
            <br/>
            <EditorMain/>
            <br/>
            <EditorBottom/>
        </div>
    );
}