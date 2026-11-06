import React from 'react'
import {useDispatch, useSelector} from 'react-redux'
import {LayerPanel} from '../LayerPanel'
import {LayerDetails, NodeI} from '../../../../../../../../common/sites/SiteModel'
import {EditorState} from '../../../../../../../EditorRootReducer'
import {LayerNetworkSwitch} from '../../../../../../../../common/model/layer/LayerNetworkSwitch'
import {TextAttribute} from '../../../element/TextAttribute'
import {AttributeDropdown} from '../../../element/AttributeDropdown'

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
        {value: '', text: '(not chosen)'},
        ...networkSwitch.paths.map(path => ({value: path.nodeId, text: `${path.label} (to ${networkIdOf(path.nodeId)})`}))
    ]

    return (
        <LayerPanel typeDisplay="Network switch" layerObject={networkSwitch}>
            {neighbourIds.map(neighbourId =>
                <TextAttribute key={key('path-' + neighbourId)} size="small" label={`Path to ${networkIdOf(neighbourId)}`}
                               value={networkSwitch.labelFor(neighbourId)}
                               save={value => networkSwitch.savePathLabel(neighbourId, value)}
                               placeholder="(not a switched path)"
                               help="The name hackers use to choose this path, for example: east. Leave empty if this connection is not part of the choice."/>
            )}

            <AttributeDropdown key={key('chosen')} label="Chosen" value={networkSwitch.chosenNodeId ?? ''} options={chosenOptions}
                               save={(value: string) => networkSwitch.saveChosen(value)}
                               tooltipId="NetworkSwitchChosen"
                               tooltipText="The path hackers have chosen. A site reset does not undo this choice, set it back to (not chosen) here."/>
        </LayerPanel>
    )
}
