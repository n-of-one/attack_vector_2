import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerTripwire} from "../../../../../../../../common/model/layer/LayerTripwire"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {EditorState} from "../../../../../../../EditorRootReducer";
import {AttributeDropdown} from "../../../element/AttributeDropdown";
import {editorCanvas} from "../../../../../canvas/EditorCanvas";
import {SELECT_LAYER} from "../../../../../../../reducer/CurrentLayerIdReducer";
import {editorSiteId} from "../../../../../../../EditorRoot";
import {createCoreOptions, createSiteOptions} from "./CoreLinkOptions";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerTripwirePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const layerTripwire = new LayerTripwire(layer, node, dispatch)
    const allCores = useSelector((state: EditorState) => state.allCores)
    const remoteSites = useSelector((state: EditorState) => state.sites)

    const coreSiteOptions = createSiteOptions(remoteSites, node.siteId, allCores)
    const coreOptions = createCoreOptions(layerTripwire.coreSiteId, allCores, node.siteId)

    const coreLayerId = layerTripwire.coreLayerId
    const navigateToLayer = () => {
        const nodeId = coreLayerId.split(":")[0]
        editorCanvas.selectNode(nodeId)
        dispatch({type: SELECT_LAYER, layerId: coreLayerId})
    }
    const coreLayer = allCores.find(coreInfo => coreInfo.layerId === coreLayerId)
    const localCore = (coreLayerId && coreLayer && coreLayer.siteId === editorSiteId)
    const navigateIfCoreId = (localCore) ? navigateToLayer : undefined


    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    const coreSiteId = layerTripwire.coreSiteId || ""

    return (
        <LayerPanel typeDisplay="Tripwire" layerObject={layerTripwire}>

            <AttributeDropdown key={key("siteId")} label="Reset site"
                               value={coreSiteId}
                               options={coreSiteOptions}
                               save={value => layerTripwire.saveCoreSiteId(value)}
                               tooltipId="coreSiteId" tooltipText="The site that contains the core that can reset this tripwire.
                               If you choose another site here, the hackers will have to go to that site in order to prevent this site from being reset."/>

            <AttributeDropdown key={key("status")} label="Reset core"
                               value={layerTripwire.coreLayerId}
                               options={coreOptions}
                               save={value => layerTripwire.saveCoreLayerId(value)}
                               tooltipId="forIce" tooltipText="The core that can reset this timer"
                               navigate={navigateIfCoreId}
            />

            <TextAttribute key={key("countdown")} size="large" label="Countdown" value={layerTripwire.countdown}
                           save={value => layerTripwire.saveCountdown(value)}
                           placeholder="(Minutes until alarm)" help="Minutes part of time until alarm."/>

            <TextAttribute key={key("shutdown")} size="large" label="Shutdown" value={layerTripwire.shutdown}
                           save={value => layerTripwire.saveShutdown(value)}
                           placeholder="(Minutes of shutdown)" help="Duration of shutdown."/>

        </LayerPanel>
    )
}
