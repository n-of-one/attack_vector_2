import React from 'react'
import {useSelector} from "react-redux"
import {TextSaveInput} from "../../../common/component/TextSaveInput"
import {sendSiteDataChanged} from "../../server/EditorServerClient"
import {EditorState} from "../../EditorRootReducer"

export const SiteDataAdditional = () => {

    const siteData = useSelector((state: EditorState) => state.siteProperties)

    const save = (field: string, value: string) => {
        sendSiteDataChanged({field, value})
    }

    return (
        <div className="row editorRow">
            <div className="col-lg-12 darkWell editorBottomPanel">
                <br/>
                <div className="row">
                    <div className="col-lg-6">
                        <div className="row form-group">
                            <label htmlFor="site_description" className="col-lg-3 control-label text-muted">Description</label>
                            <div className="col-lg-8">
                                <TextSaveInput type="textArea" id="site_description" rows={2} className="form-control editorDescription" placeholder="For GM only"
                                               value={siteData.description} save={(value) => save("description", value)}/>
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-6">
                        <div className="row form-group">
                            <label htmlFor="gm_name" className="col-lg-3 control-label text-muted">Creator</label>
                            <div className="col-lg-8">
                                <TextSaveInput id="gm_name" className="form-control" placeholder="" value={siteData.creator} save={(value) => save("creator", value)}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
