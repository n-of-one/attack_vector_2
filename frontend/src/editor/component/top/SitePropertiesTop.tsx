import React from 'react'
import {useSelector} from "react-redux"
import {TextSaveInput} from "../../../common/component/TextSaveInput"
import {CheckboxSaveInput} from "../../../common/component/CheckBoxSaveInput"
import {EditorState} from "../../EditorRootReducer"
import {sendSitePropertyChanged} from "../../server/EditorServerClient"


export const SitePropertiesTop = () => {

    const siteProperties = useSelector((state: EditorState) => state.siteProperties)

    const save = (field: string, value: string| boolean) => {
        sendSitePropertyChanged({ field, value})
    }

    return (
        <span>
            <div className="row">
                <div className="col-lg-7">
                    <div className="row form-group">
                            <label htmlFor="site_name" className="col-lg-4 control-label text-muted">Name</label>
                            <div className="col-lg-8">
                                <TextSaveInput id="site_name" className="form-control"
                                               placeholder="Display name" value={siteProperties.name}
                                               save={(value: string) => save("name", value)}/>
                            </div>
                    </div>
                    <div className="row form-group">
                            <label htmlFor="start_node" className="col-lg-4 control-label text-muted">Start node</label>
                            <div className="col-lg-5">
                                <TextSaveInput id="start_node" className="form-control" placeholder="Network Id"
                                               value={siteProperties.startNodeNetworkId}
                                               save={(value: string) => save("startNode", value)}/>
                            </div>
                    </div>
                </div>
                <div className="col-lg-5">
                    <div className="row form-group">
                            <label htmlFor="hack_time" className="col-lg-5 control-label text-muted">Hack time</label>
                            <div className="col-lg-5">
                                <TextSaveInput id="hack_time" className="form-control" placeholder="(mm:ss)"
                                               value={siteProperties.hackTime}
                                               save={(value: string) => save("hackTime", value)}/>
                            </div>
                    </div>
                    <div className="row form-group">
                            <label htmlFor="site_hackable"
                                   className="col-lg-5 control-label text-muted">Hackable</label>
                            <div className="col-lg-2">
                                <CheckboxSaveInput id="site_hackable" className="form-check-input input-checkbox"
                                                   checked={siteProperties.hackable}
                                                   save={(value: boolean) => save("hackable", value)}/>
                            </div>
                    </div>
                </div>
            </div>
    </span>
    )
}
