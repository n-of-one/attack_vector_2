import {Layer} from './Layer'
import {LayerDetails, NetworkSwitchRoute, NodeI} from '../../sites/SiteModel'
import {Dispatch} from 'redux'

export class LayerNetworkSwitch extends Layer {

    routes: NetworkSwitchRoute[]
    chosenNodeId: string | null

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.routes = layer.routes ?? []
        this.chosenNodeId = layer.chosenNodeId ?? null
    }

    nameFor(nodeId: string): string {
        return this.routes.find(route => route.nodeId === nodeId)?.name ?? ''
    }

    isSwitched(nodeId: string): boolean {
        return this.routes.some(route => route.nodeId === nodeId)
    }

    saveRouteName(nodeId: string, routeName: string) {
        super._save('ROUTE_NAME_FOR_' + nodeId, routeName)
    }

    saveRouteSwitched(nodeId: string, switched: boolean) {
        super._save('ROUTE_SWITCHED_FOR_' + nodeId, switched.toString())
    }

    saveChosen(nodeId: string) {
        super._save('CHOSEN', nodeId)
    }
}
