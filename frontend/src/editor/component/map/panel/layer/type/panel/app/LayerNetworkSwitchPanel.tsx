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

    const chosenOptions = [
        {value: '', text: '(not locked)'},
        ...networkSwitch.paths.map(path => ({value: path.nodeId, text: `${path.label || '(no name)'} → ${networkIdOf(path.nodeId)}`}))
    ]

    return (
        <LayerPanel typeDisplay="Network switch" layerObject={networkSwitch}>
            {neighbourIds.map(neighbourId =>
                <RouteRow key={key('path-' + neighbourId)} networkSwitch={networkSwitch} nodeId={neighbourId} networkId={networkIdOf(neighbourId)}/>
            )}

            <AttributeDropdown key={key('chosen')} label="Locked to" value={networkSwitch.chosenNodeId ?? ''} options={chosenOptions}
                               save={(value: string) => networkSwitch.saveChosen(value)}
                               tooltipId="NetworkSwitchChosen"
                               tooltipText="The route hackers committed to. A site reset does not unlock the switch, set it back to (not locked) here."/>
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
                                   save={(value: boolean) => networkSwitch.savePathSwitched(nodeId, value)}/>
            </div>
            <div className="col-lg-4 noRightPadding">
                <TextSaveInput className="form-control input-sm" value={networkSwitch.labelFor(nodeId)}
                               placeholder={switched ? 'choose route name' : '(not switched)'}
                               type={TextSaveType.TEXT}
                               save={value => networkSwitch.savePathLabel(nodeId, value)}/>
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
