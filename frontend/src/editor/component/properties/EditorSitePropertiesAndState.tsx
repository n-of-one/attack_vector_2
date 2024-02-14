import React from 'react'
import {SitePropertiesTop} from "./SitePropertiesTop"
import {SiteState} from "./SiteState"

export const EditorSitePropertiesAndState = () => {
    return (
        <div className="row marginTop" style={{width: "1891px"}}>
            <div className="col-lg-12 darkWell marginLeftRight">
                <div className="row marginTop">
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

