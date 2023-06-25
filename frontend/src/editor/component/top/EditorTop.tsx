import React from 'react'
import {SitePropertiesTop} from "./SitePropertiesTop"
import {SiteState} from "./SiteState"

export const EditorTop = () => {
    return (
        <div className="row editorRow">
            <div className="col-lg-12 darkWell">
                <br/>
                <div className="row">
                    <div className="col-lg-6">
                        <SitePropertiesTop/>
                    </div>
                    <div className="col-lg-6">
                        <SiteState/>
                    </div>
                </div>
            </div>
        </div>
    )
}
