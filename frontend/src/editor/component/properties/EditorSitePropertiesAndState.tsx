import React from 'react'
import {SiteProperties} from "./SiteProperties"
import {SiteState} from "./SiteState"

export const EditorSitePropertiesAndState = () => {
    return (
        <div className="row marginTop" style={{width: "1891px"}}>
            <div className="col-lg-12 darkWell marginLeftRight">
                <div className="row marginTop">
                    <div className="col-lg-6">
                        <SiteProperties/>
                    </div>
                    <div className="col-lg-6">
                        <SiteState/>
                    </div>
                </div>
            </div>
        </div>
    )
}

