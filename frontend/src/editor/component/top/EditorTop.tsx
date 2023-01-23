import React from 'react'
import {SiteData} from "./SiteData"
import {SiteState} from "./SiteState"

export const EditorTop = () => {
    console.log("render EditorTop")
    return (
        <div className="row editorRow">
            <div className="col-lg-12 darkWell">
                <br/>
                <div className="row">
                    <div className="col-lg-6">
                        <SiteData/>
                    </div>
                    <div className="col-lg-6">
                        <SiteState/>
                    </div>
                </div>
            </div>
        </div>
    )
}
