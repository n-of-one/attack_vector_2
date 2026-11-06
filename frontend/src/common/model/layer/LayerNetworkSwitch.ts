import {Layer} from './Layer'
import {LayerDetails, NetworkSwitchRoute, NodeI} from '../../sites/SiteModel'
import {Dispatch} from 'redux'

export class LayerNetworkSwitch extends Layer {

    routes: NetworkSwitchRoute[]
    lockedRouteNodeId: string | null
    coreLayerId: string
    coreSiteId?: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.routes = layer.routes ?? []
        this.lockedRouteNodeId = layer.lockedRouteNodeId ?? null
        this.coreLayerId = layer.coreLayerId ?? ''
        this.coreSiteId = layer.coreSiteId
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

    saveLockedRouteNodeId(nodeId: string) {
        super._save('LOCKED_ROUTE_NODE_ID', nodeId)
    }

    saveCoreLayerId(value: string) {
        super._save('CORE_LAYER_ID', value)
    }

    saveCoreSiteId(value: string) {
        super._save('CORE_SITE_ID', value)
    }
}
