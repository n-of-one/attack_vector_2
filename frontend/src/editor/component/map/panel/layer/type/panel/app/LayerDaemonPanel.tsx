import React from 'react'
import {useDispatch} from 'react-redux'
import {TextAttribute} from '../../../element/TextAttribute'
import {LayerPanel} from '../LayerPanel'
import {LayerDetails, NodeI} from '../../../../../../../../common/sites/SiteModel'
import {TextSaveType} from '../../../../../../../../common/component/TextSaveInput'
import {AttributeTerminalPreview} from '../../../element/AttributeTerminalPreview'
import {TextEditorLink} from '../../../element/TextEditorLink'
import {AttributeDropdown} from '../../../element/AttributeDropdown'
import {LayerDaemon} from '../../../../../../../../common/model/layer/LayerDaemon'

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerDaemonPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const daemon = new LayerDaemon(layer, node, dispatch)

    // Same as the line the server prints when a hacker arrives in a node (CommandMoveService.arriveComplete)
    const nodeName = node.layers[0]?.nodeName
    const nodeNameText = nodeName ? `[/]: ${nodeName}` : '[/]'
    const enteredText = `Entered node [ok]${node.networkId}${nodeNameText}\n`

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ':' + param

    return (
        <LayerPanel typeDisplay="Daemon" layerObject={daemon}>
            <TextAttribute key={key('requiredTrait')} size="medium" label="Required trait" value={daemon.requiredTrait}
                           save={value => daemon.saveRequiredTrait(value)}
                           placeholder="(any hacker)"
                           help="Only hackers that have a Trait skill with this key will see the text. Leave empty to show the text to every hacker."/>

            <AttributeDropdown label="Recurring" value={daemon.recurring.toString()} options={
                [{value: 'FIRST_ENTRY_ONLY', text: 'First entry only (any hacker)'},
                    {value: 'EACH_HACKER_ONCE', text: 'Each hacker once'},
                    {value: 'EVERY_ENTRY', text: 'Every entry'},
                ]} save={(value: string) => daemon.saveRecurring(value)}
                               tooltipText="When the text is shown: only to the first hacker that enters, once for each hacker, or every time a hacker enters."
                               tooltipId="DaemonRecurring"/>

            <TextAttribute key={key('text')} size="large" label="Text on entry" value={daemon.text}
                           save={value => daemon.saveText(value)}
                           placeholder="* Connection logged."
                           help="This text is shown in the terminal of a hacker that enters this node.
                              Links to web pages or images can be added like this: [http://example.com]click here[/]"
                           type={TextSaveType.TEXTAREA} sendEvent={true}
            />
            <TextEditorLink node={node} layer={layer} keyText="TEXT" size="large" label=""/>
            <AttributeTerminalPreview prefix={enteredText} width="616px"/>
        </LayerPanel>
    )
}
