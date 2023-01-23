import React from 'react'
import {EditorMain} from "./middle/EditorMain"
import {EditorTop} from "./top/EditorTop"
import {SiteDataAdditional} from "./bottom/SiteDataAdditional"

export const EditorHome = () => {
    return (
        <div className="container-fluid" data-bs-theme="dark">
            <br/>
            <EditorTop/>
            <br/>
            <EditorMain/>
            <br/>
            <SiteDataAdditional/>
        </div>
    )
}