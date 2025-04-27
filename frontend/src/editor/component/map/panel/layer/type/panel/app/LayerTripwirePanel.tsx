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
import {SiteInfo} from "../../../../../../../../common/sites/SitesReducer";
import userAuthorizations, {ROLE_GM} from "../../../../../../../../common/user/UserAuthorizations";
import {CoreInfo} from "../../../../../../../reducer/AllCoresReducer";

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
    const navigateIfCoreId = (coreLayerId) ? navigateToLayer : undefined


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

const determineSitesWithCoreIds = (allCores: CoreInfo[]): string[] => {
    const allSiteIds = allCores.map(siteInfo => siteInfo.siteId)
    return [...new Set(allSiteIds)];
}

const createCoreOptions = (chosenCoreSiteIdInput: string | undefined, allCores: CoreInfo[], currentSiteId: string) => {
    const chosenCoreSiteId = chosenCoreSiteIdInput ? chosenCoreSiteIdInput : currentSiteId
    const coresOfChosenSite = allCores.filter((coreInfo: CoreInfo) => coreInfo.siteId === chosenCoreSiteId)

    const remoteIndicator = (chosenCoreSiteId !== currentSiteId) ? "Remote " : ""

    const siteCoreOptions = coresOfChosenSite.map((coreInfo: CoreInfo) => {
        return {
            value: coreInfo.layerId,
            text: `${remoteIndicator}${coreInfo.networkId}:${coreInfo.level} ${coreInfo.name}`
        }
    })

    return [{value: "", text: "Choose Core"}, ...siteCoreOptions]
}

const createSiteOptions = (remoteSites: Array<SiteInfo>, currentSiteId: string, allCores: CoreInfo[]) => {
    const isGm = userAuthorizations.hasRole(ROLE_GM)

    const remoteSitesFiltered = remoteSites.filter((siteInfo: SiteInfo) => {
        return isGm || siteInfo.mine // hackers can only see their own sites
    })

    const sitesWithCoreIds: string[] = determineSitesWithCoreIds(allCores)
    const remoteSitesWithCores = remoteSitesFiltered.filter((siteInfo: SiteInfo) => sitesWithCoreIds.includes(siteInfo.id))

    const remoteSitesSorted = sortRemoteSites(remoteSitesWithCores, currentSiteId)

    return remoteSitesSorted.map((remoteSite) => {
        return {
            value: remoteSite.id,
            text: remoteSite.id === currentSiteId ? "This site" : `${remoteSite.name} (${remoteSite.ownerName})`,
        }
    })
}

const sortRemoteSites = (remoteSites: Array<SiteInfo>, currentSiteId: string) => {
    remoteSites.sort((a: SiteInfo, b: SiteInfo) => {
        if (a.id === currentSiteId) return -1
        if (b.id === currentSiteId) return 1

        if (a.mine && !b.mine) return -1
        if (b.mine && !a.mine) return 1

        if (a.gmSite && !b.gmSite) return -1
        if (b.gmSite && !a.gmSite) return 1

        return a.name.localeCompare(b.name)
    })

    return remoteSites
}
