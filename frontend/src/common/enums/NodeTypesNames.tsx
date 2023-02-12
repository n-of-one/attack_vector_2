import {
    DATA_STORE, ICE_1, ICE_2,ICE_3, MANUAL_1, MANUAL_2, MANUAL_3,
    PASSCODE_STORE, RESOURCE_STORE, SYSCON, UNHACKABLE,
    TRANSIT_1,TRANSIT_2, TRANSIT_3, TRANSIT_4,
    NodeType,
} from "../../editor/reducer/NodesReducer";

export class NodeFileType {
    name: NodeType
    dir: string

    constructor(name: NodeType,dir: string) {
        this.name = name
        this.dir = dir
    }
}

export const TYPE_TRANSIT_1 = new NodeFileType(TRANSIT_1, "reg")
export const TYPE_TRANSIT_2 = new NodeFileType(TRANSIT_2, "reg")
export const TYPE_TRANSIT_3 = new NodeFileType(TRANSIT_3, "reg")
export const TYPE_TRANSIT_4 = new NodeFileType(TRANSIT_4, "reg")
export const TYPE_SYSCON = new NodeFileType(SYSCON, "reg")
export const TYPE_DATA_STORE = new NodeFileType(DATA_STORE, "reg")
export const TYPE_PASSCODE_STORE = new NodeFileType(PASSCODE_STORE, "reg")
export const TYPE_RESOURCE_STORE = new NodeFileType(RESOURCE_STORE, "reg")
export const TYPE_ICE_1 = new NodeFileType(ICE_1, "ice")
export const TYPE_ICE_2 = new NodeFileType(ICE_2, "ice")
export const TYPE_ICE_3 = new NodeFileType(ICE_3, "ice")
export const TYPE_UNHACKABLE = new NodeFileType(UNHACKABLE, "ice")
export const TYPE_MANUAL_1 = new NodeFileType(MANUAL_1, "ice")
export const TYPE_MANUAL_2 = new NodeFileType(MANUAL_2, "ice")
export const TYPE_MANUAL_3 = new NodeFileType(MANUAL_3, "ice")
const all: {[key: string] : NodeFileType}  = {
    TRANSIT_1: TYPE_TRANSIT_1, TRANSIT_2: TYPE_TRANSIT_2, TRANSIT_3: TYPE_TRANSIT_3, TRANSIT_4: TYPE_TRANSIT_4, SYSCON: TYPE_SYSCON, DATA_STORE: TYPE_DATA_STORE,
    PASSCODE_STORE: TYPE_PASSCODE_STORE, RESOURCE_STORE: TYPE_RESOURCE_STORE, ICE_1: TYPE_ICE_1, ICE_2: TYPE_ICE_2, ICE_3: TYPE_ICE_3,
    UNHACKABLE: TYPE_UNHACKABLE, MANUAL_1: TYPE_MANUAL_1, MANUAL_2: TYPE_MANUAL_2, MANUAL_3: TYPE_MANUAL_3
}

export const toType = (name: NodeType): NodeFileType => {
    const match = Object.entries(all).find(
        ([key, _]) => {
            return key === name
        }
    )
    if (match) {
        return match[1]
    }
    throw new Error("No node type exists for: " + name)
}
