import React from 'react'
import {useDispatch} from "react-redux"
import {LayerField} from "../../../LayerField"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerStatusLight} from "../../../../../../../../common/model/layer/LayerStatusLight";
import {LayerFieldDropdown} from "../../../LayerFieldDropdown";
import QRCode from "react-qr-code"
import {OverlayTrigger, Tooltip} from "react-bootstrap";

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
            <LayerFieldDropdown key={key("status")} label="Status"
                                value={"" + statusLight.status}
                                options={[ {value: "false", text: `red:${statusLight.textForRed}`},
                                    {value: "true", text: `green: ${statusLight.textForGreen}`}]}
                                save={value => statusLight.saveStatus(value)}
                                tooltipId="status_options" tooltipText="Current status"/>

            <LayerField key={key("textForRed")} size="large" label="Text for red" value={statusLight.textForRed}
                        save={value => statusLight.saveTextForRed(value)}
                        help="Shown in the switch to indicate what this position means"/>

            <LayerField key={key("textForGreen")} size="large" label="Text for green" value={statusLight.textForGreen}
                        save={value => statusLight.saveTextForGreen(value)}
                        help="Shown in the switch to indicate what this position means"/>

            <LayerField key={key("appId")} size="large" label="AppId" value={statusLight.appId} readOnly={true}
                        help="ID of Status Light App."/>

            <div className="row form-group layerFieldRow">
                <div className="col-lg-3 layerLabel">QR</div>
                <div className="col-lg-5 noRightPadding">
                    <div style={{}}>
                    <QRCode size={256} value={"http://carcosa/widget/" + statusLight.appId} viewBox="0 0 256 256"/>
                    </div>

                </div>
                <div className="col-lg-1 layerHelpColumn">
                    <OverlayTrigger
                        key={"tooltip_qr"}
                        placement="left"
                        overlay={
                            <Tooltip id={`tooltip-qr`}>
                                <span >Scan QR</span>
                            </Tooltip>
                        }
                    >
                        <span className="badge bg-secondary helpBadge">?</span>
                    </OverlayTrigger>
                </div>
            </div>


        </LayerPanel>
    )
}
