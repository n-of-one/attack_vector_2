import React from 'react'
import {EditorMain} from "./map/EditorMain"
import {EditorSitePropertiesAndState} from "./properties/EditorSitePropertiesAndState"
import {SiteDescription} from "./description/SiteDescription"
import {NodesPanel} from "./nodes/NodesPanel";

export const EditorHome = () => {
    return (
        <div className="container-fluid" data-bs-theme="dark">
            <EditorSitePropertiesAndState/>
            <NodesPanel/>
            <EditorMain/>
            <SiteDescription/>
        </div>
    )
}