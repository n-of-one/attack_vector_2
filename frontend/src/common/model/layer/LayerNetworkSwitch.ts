import {Layer} from './Layer'
import {LayerDetails, NetworkSwitchPath, NodeI} from '../../sites/SiteModel'
import {Dispatch} from 'redux'

export class LayerNetworkSwitch extends Layer {

    paths: NetworkSwitchPath[]
    chosenNodeId: string | null

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.paths = layer.paths ?? []
        this.chosenNodeId = layer.chosenNodeId ?? null
    }

    labelFor(nodeId: string): string {
        return this.paths.find(path => path.nodeId === nodeId)?.label ?? ''
    }

    savePathLabel(nodeId: string, label: string) {
        super._save('PATH_LABEL_FOR_' + nodeId, label)
    }

    saveChosen(nodeId: string) {
        super._save('CHOSEN', nodeId)
    }
}
