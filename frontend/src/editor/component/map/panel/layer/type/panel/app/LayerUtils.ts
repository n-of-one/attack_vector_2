import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";

export type DropdownOption = {
    value: string,
    text: string,
}

export interface FilterInput {
    networkId: string,
    layer: LayerDetails
}

type FilterFunction = (filterInput: FilterInput) => boolean

const filterIce: FilterFunction = (filterInput: FilterInput) => filterInput.layer.ice

export const iceLayerOptions = (nodes: Array<NodeI>): DropdownOption[] => {
    return layerOptions(nodes, filterIce, "Choose Ice")
}


export const layerOptions = (nodes: Array<NodeI>, filterFunction: FilterFunction, defaultText: string): DropdownOption[] => {
    const iceLayers = nodes
        .map(node => {
            return node.layers.map((layer: LayerDetails) => { return {networkId: node.networkId, layer: layer}})
        })
        .flat()
        .filter(filterFunction)
        .sort((a, b) => a.layer.level - b.layer.level)
        .sort((a, b) => a.networkId.localeCompare(b.networkId))
        .map(layerWithNetwork => {
            const location = layerWithNetwork.networkId + ":" + layerWithNetwork.layer.level
            return {
                value: layerWithNetwork.layer.id,
                text: `${location} ${layerWithNetwork.layer.name} ${layerWithNetwork.layer.type}`,
            }
        })
    return [{value: "", text: defaultText}, ...iceLayers ]
}
