import React from 'react'
import {useDispatch} from "react-redux"
import {LayerField} from "../../../LayerField"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerStatusLight} from "../../../../../../../../common/model/layer/LayerStatusLight";
import {LayerFieldDropdown} from "../../../LayerFieldDropdown";
import QRCode from "react-qr-code"
import toast, {Toast} from "react-hot-toast";





interface Props {
    node: NodeI,
    layer: LayerDetails
}


export const LayerStatusLightPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const statusLight = new LayerStatusLight(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    const widgetUrl = `${window.location.origin}/widget/${statusLight.appId}`
    const appUrl = `${window.location.origin}/app/${statusLight.appId}`



    return (
        <LayerPanel typeDisplay="StatusLight" layerObject={statusLight}>
            <LayerFieldDropdown key={key("status")} label="Status"
                                value={"" + statusLight.status}
                                options={[{value: "false", text: `red:${statusLight.textForRed}`},
                                    {value: "true", text: `green: ${statusLight.textForGreen}`}]}
                                save={value => statusLight.saveStatus(value)}
                                tooltipId="status_options" tooltipText="Current status"/>

            <LayerField key={key("textForRed")} size="large" label="Text for red" value={statusLight.textForRed}
                        save={value => statusLight.saveTextForRed(value)}
                        help="Shown in the switch to indicate what this position means"/>

            <LayerField key={key("textForGreen")} size="large" label="Text for green" value={statusLight.textForGreen}
                        save={value => statusLight.saveTextForGreen(value)}
                        help="Shown in the switch to indicate what this position means"/>

            <UrlFieldWithQr name="App" url={appUrl} description="App for changing status"/>

            <UrlFieldWithQr name="Widget" url={widgetUrl} description="Widget showing status"/>

        </LayerPanel>
    )
}

interface UrlFieldWithQrProps {
    name: string,
    url: string,
    description: string
}

const UrlFieldWithQr = ({name, url, description}: UrlFieldWithQrProps) => {

    const showQR = () => {
        toast((t: Toast) => {
                const dismissMethod = () => toast.dismiss(t.id)
                return (
                    <div>
                        <div>{description}</div>
                        <div>&nbsp;</div>
                        <div>{url}</div>
                        <div onClick={dismissMethod}><br/>
                            <QRCode size={256} value={url} viewBox="0 0 256 256"/>
                            <div>&nbsp;</div>
                            <div>Click to close</div>
                        </div>
                    </div>)
            },
            {
                duration: 0,
                style: {
                    borderRadius: '5px', background: '#fff', color: '#333', border: '2px solid #000', maxWidth: '900px',
                },
            })
    }

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{name}</div>
            <div className="col-lg-8 noRightPadding">
                    <span>
                        <input type="text" className="form-control input-sm" readOnly={true}
                               value={url}
                               onClick={(e) => {
                                   window.open(url, "_blank")
                               }}
                               style={{textDecoration: "underline", color: "steelblue"}}
                        />
                    </span>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <div className="btn btn-info btn-sm" onClick={showQR}>QR</div>
            </div>
        </div>
    )
}