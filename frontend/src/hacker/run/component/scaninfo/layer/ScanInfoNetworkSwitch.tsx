import React from 'react'
import {useSelector} from 'react-redux'
import {LayerDetails} from '../../../../../common/sites/SiteModel'
import {HackerRootState} from '../../../../HackerRootReducer'

export const ScanInfoNetworkSwitch = ({layer}: { layer: LayerDetails }) => {
    const nodes = useSelector((state: HackerRootState) => state.run.site.nodes)
    const networkIdOf = (nodeId: string) => nodes.find(node => node.id === nodeId)?.networkId ?? '??'

    const paths = layer.paths ?? []
    const chosen = paths.find(path => path.nodeId === layer.chosenNodeId)
    if (chosen) {
        return <> - set to: <span className="text-info">{chosen.label}</span> ({networkIdOf(chosen.nodeId)})</>
    }
    const choices = paths.map(path => `${path.label} (${networkIdOf(path.nodeId)})`).join(', ')
    return <> - choose: <span className="text-info">{choices}</span></>
}
