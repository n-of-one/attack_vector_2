import React from 'react'
import {useDispatch, useSelector} from 'react-redux'
import {LayerPanel} from '../LayerPanel'
import {LayerDetails, NodeI} from '../../../../../../../../common/sites/SiteModel'
import {EditorState} from '../../../../../../../EditorRootReducer'
import {LayerNetworkSwitch} from '../../../../../../../../common/model/layer/LayerNetworkSwitch'
import {AttributeDropdown} from '../../../element/AttributeDropdown'
import {TextSaveInput, TextSaveType} from '../../../../../../../../common/component/TextSaveInput'
import {OverlayTrigger, Tooltip} from 'react-bootstrap'
import {CheckboxSaveInput} from '../../../../../../../../common/component/CheckBoxSaveInput'
import {createCoreOptions, createSiteOptions} from './CoreLinkOptions'
import {editorSiteId} from '../../../../../../../EditorRoot'
import {editorCanvas} from '../../../../../canvas/EditorCanvas'
import {SELECT_LAYER} from '../../../../../../../reducer/CurrentLayerIdReducer'

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerNetworkSwitchPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const networkSwitch = new LayerNetworkSwitch(layer, node, dispatch)
    const nodes = useSelector((state: EditorState) => state.nodes)
    const connections = useSelector((state: EditorState) => state.connections)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ':' + param

    const networkIdOf = (nodeId: string) => nodes.find(it => it.id === nodeId)?.networkId ?? '?'

    const neighbourIds = connections
        .filter(connection => connection.fromId === node.id || connection.toId === node.id)
        .map(connection => connection.fromId === node.id ? connection.toId : connection.fromId)
        .sort((a, b) => networkIdOf(a).localeCompare(networkIdOf(b)))

    const lockedRouteOptions = [
        {value: '', text: '(not locked)'},
        ...networkSwitch.routes.map(route => ({value: route.nodeId, text: `${route.name || '(no name)'} → ${networkIdOf(route.nodeId)}`}))
    ]

    const allCores = useSelector((state: EditorState) => state.allCores)
    const sites = useSelector((state: EditorState) => state.sites)
    const coreSiteOptions = createSiteOptions(sites, node.siteId, allCores)
    const coreOptions = createCoreOptions(networkSwitch.coreSiteId, allCores, node.siteId)

    const coreLayerId = networkSwitch.coreLayerId
    const coreLayer = allCores.find(coreInfo => coreInfo.layerId === coreLayerId)
    const navigateToCore = (coreLayerId && coreLayer && coreLayer.siteId === editorSiteId) ? () => {
        editorCanvas.selectNode(coreLayerId.split(':')[0])
        dispatch({type: SELECT_LAYER, layerId: coreLayerId})
    } : undefined

    return (
        <LayerPanel typeDisplay="Network switch" layerObject={networkSwitch}>
            {neighbourIds.map(neighbourId =>
                <RouteRow key={key('route-' + neighbourId)} networkSwitch={networkSwitch} nodeId={neighbourId} networkId={networkIdOf(neighbourId)}/>
            )}

            <AttributeDropdown key={key('lockedRoute')} label="Locked to" value={networkSwitch.lockedRouteNodeId ?? ''} options={lockedRouteOptions}
                               save={(value: string) => networkSwitch.saveLockedRouteNodeId(value)}
                               tooltipId="NetworkSwitchLockedRoute"
                               tooltipText="The route hackers committed to. A site reset does not unlock the switch, set it back to (not locked) here."/>

            <AttributeDropdown key={key('coreSiteId')} label="Unlock site" value={networkSwitch.coreSiteId || ''} options={coreSiteOptions}
                               save={(value: string) => networkSwitch.saveCoreSiteId(value)}
                               tooltipId="NetworkSwitchCoreSite"
                               tooltipText="The site that contains the core that can unlock this switch. If you choose another site here,
                               the hackers will have to go to that site to unlock it."/>

            <AttributeDropdown key={key('coreLayerId')} label="Unlock core" value={coreLayerId} options={coreOptions}
                               save={(value: string) => networkSwitch.saveCoreLayerId(value)}
                               tooltipId="NetworkSwitchCore"
                               tooltipText="Hacking this core unlocks the switch, so hackers can choose a route again. Optional."
                               navigate={navigateToCore}/>
        </LayerPanel>
    )
}

const RouteRow = ({networkSwitch, nodeId, networkId}: { networkSwitch: LayerNetworkSwitch, nodeId: string, networkId: string }) => {
    const switched = networkSwitch.isSwitched(nodeId)

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">Route → {networkId}</div>
            <div className="col-lg-1 noRightPadding">
                <CheckboxSaveInput id={'route-switched-' + nodeId} className="form-control form-check-input" checked={switched}
                                   save={(value: boolean) => networkSwitch.saveRouteSwitched(nodeId, value)}/>
            </div>
            <div className="col-lg-4 noRightPadding">
                <TextSaveInput className="form-control input-sm" value={networkSwitch.nameFor(nodeId)}
                               placeholder={switched ? 'choose route name' : '(not switched)'}
                               type={TextSaveType.TEXT}
                               save={value => networkSwitch.saveRouteName(nodeId, value)}/>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key={'tooltip_route_' + nodeId}
                    placement="left"
                    overlay={
                        <Tooltip id={'tooltip-route-' + nodeId}>
                            Check to make this route part of the switch. The route name is what hackers type to choose it, e.g. "research".
                        </Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>
        </div>
    )
}
