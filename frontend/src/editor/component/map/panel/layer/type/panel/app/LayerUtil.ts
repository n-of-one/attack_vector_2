import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {LayerType} from "../../../../../../../../common/enums/LayerTypes";

export type DropdownOption = {
    value: string,
    text: string,
}

interface FilterInput {
    networkId: string,
    layer: LayerDetails
}

type FilterFunction = (filterInput: FilterInput) => boolean

const filterIce: FilterFunction = (filterInput: FilterInput) => filterInput.layer.ice
const filterCore = (filterInput: FilterInput) => filterInput.layer.type === LayerType.CORE

export const iceLayerOptions = (nodes: Array<NodeI>): DropdownOption[] => {
    return layerOptions(nodes, filterIce, "Choose Ice")
}

export const coreLayerOptions = (nodes: Array<NodeI>): DropdownOption[] => {
    return layerOptions(nodes, filterCore, "Choose Core")
}

const layerOptions = (nodes: Array<NodeI>, filterFunction: FilterFunction, defaultText: string): DropdownOption[] => {
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
