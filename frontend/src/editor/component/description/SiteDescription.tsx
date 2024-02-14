import React from 'react'
import {useSelector} from "react-redux"
import {TextSaveInput, TextSaveType} from "../../../common/component/TextSaveInput"
import {sendSitePropertyChanged} from "../../server/EditorServerClient"
import {EditorState} from "../../EditorRootReducer"

export const SiteDescription = () => {

    const siteProperties = useSelector((state: EditorState) => state.siteProperties)

    const save = (field: string, value: string) => {
        sendSitePropertyChanged({field, value})
    }

    return (
        <div className="row" style={{width: "1891px"}}>
            <div className="col-lg-12 marginTop marginLeftRight darkWell editorBottomPanel" id="node-library">
                <br/>
                <div className="row form-group">
                    <label htmlFor="site_description" className="col-lg-1 control-label text-muted">Description</label>
                    <div className="col-lg-11">
                        <TextSaveInput type={TextSaveType.TEXTAREA} id="site_description" rows={2} className="form-control editorDescription" placeholder="For GM only"
                                       value={siteProperties.description} save={(value) => save("description", value)}/>
                    </div>
                </div>
            </div>
        </div>
    )
}
