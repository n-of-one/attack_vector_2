import React from 'react'
import {useSelector} from 'react-redux'
import {LayerDetails} from '../../../../../common/sites/SiteModel'
import {HackerRootState} from '../../../../HackerRootReducer'

export const ScanInfoNetworkSwitch = ({layer}: { layer: LayerDetails }) => {
    const nodes = useSelector((state: HackerRootState) => state.run.site.nodes)
    const networkIdOf = (nodeId: string) => nodes.find(node => node.id === nodeId)?.networkId ?? '??'

    const routes = layer.routes ?? []
    const chosen = routes.find(route => route.nodeId === layer.chosenNodeId)
    if (chosen) {
        return <> - set to: <span className="text-info">{chosen.name}</span> ({networkIdOf(chosen.nodeId)})</>
    }
    const choices = routes.map(route => `${route.name} (${networkIdOf(route.nodeId)})`).join(', ')
    return <> - choose: <span className="text-info">{choices}</span></>
}
