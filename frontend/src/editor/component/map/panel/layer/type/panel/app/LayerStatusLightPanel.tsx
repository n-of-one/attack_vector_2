import React from 'react'
import {useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {LayerStatusLight} from "../../../../../../../../common/model/layer/LayerStatusLight";
import {AttributeUrlWithQr} from "../../../element/AttributeUrlWithQr";
import {TextSaveInput, TextSaveType} from "../../../../../../../../common/component/TextSaveInput";
import {OverlayTrigger, Tooltip} from "react-bootstrap";
import {StatusLightOption} from "../../../../../../../../standalone/widget/statusLight/StatusLightReducers";
import {SilentLink} from "../../../../../../../../common/component/SilentLink";
import {Glyphicon} from "../../../../../../../../common/component/icon/Glyphicon";
import {TextAttribute} from "../../../element/TextAttribute";


interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerStatusLightPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const statusLight = new LayerStatusLight(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param


    return (
        <LayerPanel typeDisplay="StatusLight" layerObject={statusLight}>

            <TextAttribute size="small" label="Switch label" value={statusLight.switchLabel} save={value => statusLight.saveSwitchLabel(value)}
                           help="Text shown to hackers when they access the switch. This can give them an indication what this switch is for, which can be different from the name of the layer."
                           id="switchLabel_id"/>
            <br/>
            <CurrentOptionDropdown statusLight={statusLight}/>

            {statusLight.options.map((option: StatusLightOption, index: number) => <AttributeStatusLightOption key={key("option" + index)}
                                                                                                               statusLight={statusLight} option={option}
                                                                                                               index={index}/>)}

            <AddOptions statusLight={statusLight}/>
            <AttributeUrlWithQr name="Switch" type="app" subType="switch" layerId={statusLight.id} description="App for changing status"
                                fileName={`node-${node.networkId}-level-${layer.level}-${layer.name}-switch`}/>

            <AttributeUrlWithQr name="Widget" type="widget" subType="statusLight" layerId={statusLight.id} description="Widget showing status"
                                requireLogin={false} fileName={`node-${node.networkId}-level-${layer.level}-${layer.name}-widget`}/>

        </LayerPanel>
    )
}

export const CurrentOptionDropdown = ({statusLight}: { statusLight: LayerStatusLight }) => {

    const currentOptionOptions = statusLight.options.map((option: StatusLightOption, index: number) => {
        return <option value={index.toString()} key={`key${index}`}>{`${index + 1}: ${option.text}`}</option>

    })

    const currentOption = statusLight.options[statusLight.currentOption] || {color: "black"}

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">Current</div>
            <div className="col-lg-1 noRightPadding">
                <span className="status-light-editor-preview" style={{"--status-light-color": currentOption.color} as React.CSSProperties}>&nbsp;</span>
            </div>
            <div className="col-lg-4 noRightPadding">
                <select className="form-control" onChange={(event) => statusLight.saveCurrentOption(event.target.value)} value={statusLight.currentOption}>
                    {currentOptionOptions}
                </select>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key="tooltip_current_option"
                    placement="left"
                    overlay={
                        <Tooltip id={"tooltipId"}>Current selected option</Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>
        </div>
    )
}

const AttributeStatusLightOption = ({statusLight, option, index}: { statusLight: LayerStatusLight, option: StatusLightOption, index: number }) => {

    const canDelete = (statusLight.options.length > 2)

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">Option {index + 1}</div>
            <div className="col-lg-5 noRightPadding">
                <TextSaveInput className="form-control input-sm" placeholder="Optional text" value={option.text} id={`optionText${index}`}
                               type={TextSaveType.TEXT}
                               save={value => statusLight.saveTextFor(index, value)}/>

            </div>
            <div className="col-lg-2 noRightPadding">
                <TextSaveInput className="form-control input-sm" placeholder="Color" value={option.color} id={`optionColor${index}`}
                               type={TextSaveType.TEXT}
                               save={value => statusLight.saveColorFor(index, value)}/>
            </div>
            <div className="col-lg-1 noRightPadding">
                <span className="status-light-editor-preview" style={{"--status-light-color": option.color} as React.CSSProperties}>&nbsp;</span>
            </div>

            {canDelete ? (<div className="col-lg-1 layerHelpColumn">
                <span className="pull-right">
                    <SilentLink onClick={() => statusLight.deleteOption(index)}>
                        <Glyphicon name="glyphicon-trash"/>
                    </SilentLink>
                </span>
            </div>) : (<div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key="tooltip_current_option"
                    placement="left"
                    overlay={<Tooltip id={"tooltipId"}>Option text is optional. Option color uses css color notation,
                        with fixed names like: red, blue, lime. Or rgb values like: #0f0 or #daa520 .</Tooltip>}>
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>)}
        </div>
    )
}

const AddOptions = ({statusLight}: { statusLight: LayerStatusLight }) => {
    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel"></div>
            <div className="col-lg-8 noRightPadding">
                <div className="btn btn-info btn-sm" onClick={() => statusLight.addOption()}>Add option</div>
                <br/>
                <br/>
            </div>
        </div>
    )
}
