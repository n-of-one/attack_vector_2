import React from 'react'
import {useDispatch} from "react-redux"
import {LayerField} from "../../../element/LayerField"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerStatusLight} from "../../../../../../../../common/model/layer/LayerStatusLight";
import {LayerFieldDropdown} from "../../../element/LayerFieldDropdown";
import QRCode from "react-qr-code"
import toast, {Toast} from "react-hot-toast";
import {encodeAppReference} from "../../../../../../../../common/util/Util";
import {UrlFieldWithQr} from "../../../element/UrlFieldWithQr";


interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerStatusLightPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const statusLight = new LayerStatusLight(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    const widgetReference = encodeAppReference(`widget/${statusLight.appId}`)
    const widgetUrl = `${window.location.origin}/${widgetReference}`

    const appReference = encodeAppReference(`app/${statusLight.appId}`)
    const appUrl = `${window.location.origin}/${appReference}`


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

