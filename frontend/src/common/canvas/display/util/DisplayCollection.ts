import {Display} from "../Display";
import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";

export class DisplayCollection<Type extends Display> {

    private readonly name: string
    private byId: { [key: string]: Type } = {}

    constructor(name: string) {
        this.name = name
    }


    add(id: string, display: Type) {
        this.byId[id] = display
    }

    get(id: string): Type {
        const result = this.byId[id]
        if (result === null || result === undefined) {
            throw new Error("Did not find " + this.name + " with id: " + id)
        }
        return result
    }

    getOrNull(id: string) {
        const result = this.byId[id]
        return (result) ? result : null
    }

    getAllIcons(): fabric.Object[] {
        return Object.values(this.byId).map(display => display.getAllIcons()).flatMap(num => num)
    }

    removeAllAndTerminate(canvas: Canvas) {
        this.getAllIcons().forEach((icon: fabric.Object) => canvas.remove(icon))
        Object.values(this.byId).forEach(display => display.terminate())
        this.byId = {}
    }

    remove(id: string) {
        delete this.byId[id]
    }

    forEach(method: (nodeDisplay: Type) => void) {
        Object.values(this.byId).forEach(method)
    }

    has(id: string) {
        const result = this.byId[id]
        return (result !== null && result !== undefined)
    }
}