import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {EditorState} from "../../../../../../../EditorRootReducer";
import {AttributeDropdown} from "../../../element/AttributeDropdown";
import {LayerUplink} from "../../../../../../../../common/model/layer/LayerUplink";
import {SiteInfo} from "../../../../../../../../common/sites/SitesReducer";
import userAuthorizations, {ROLE_GM} from "../../../../../../../../common/user/UserAuthorizations";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerUplinkPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const layerUplink = new LayerUplink(layer, node, dispatch)
    const sites = useSelector((state: EditorState) => state.sites)

    const options = createTargetSiteOptions(sites, node.siteId, layerUplink)

    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="Uplink" layerObject={layerUplink}>
            <AttributeDropdown key={key("targetSiteId")} label="Target site"
                               value={layerUplink.targetSiteId}
                               options={options}
                               save={value => layerUplink.saveTargetSiteId(value)}
                               tooltipId="targetSiteId" tooltipText="Hacking this layer connects the hacker to the start node of this site.
                               Make the target site internal, so it can only be reached via an uplink."/>
        </LayerPanel>
    )
}

const createTargetSiteOptions = (sites: SiteInfo[], currentSiteId: string, layer: LayerUplink) => {
    const isGm = userAuthorizations.hasRole(ROLE_GM)
    const targetSites = sites
        .filter(site => site.id !== currentSiteId && (isGm || site.mine)) // hackers can only see their own sites
        .sort((a, b) => {
            if (a.mine !== b.mine) return a.mine ? -1 : 1
            if (a.internal !== b.internal) return a.internal ? -1 : 1
            return a.name.localeCompare(b.name)
        })

    const options = targetSites.map(site => ({
        value: site.id,
        text: `${site.name} (${site.ownerName})${site.internal ? "" : " - not internal"}`,
    }))

    if (layer.targetSiteId && !targetSites.some(site => site.id === layer.targetSiteId)) {
        options.push({value: layer.targetSiteId, text: `${layer.targetSiteName} (not found)`})
    }

    return [{value: "", text: "Choose site"}, ...options]
}
