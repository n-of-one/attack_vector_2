import {fabric} from "fabric";

export default class TangleLineDisplay {


    constructor(canvas, id, point1, point2) {
        this.canvas = canvas;
        this.id = id;
        this.point1 = point1;
        this.point2 = point2;

        this.icon = new fabric.Line(
            [this.point1.icon.left, this.point1.icon.top, this.point2.icon.left, this.point2.icon.top], {
                stroke: "#000",
                strokeWidth: 2,
                selectable: false,
                hoverCursor: 'default',
            });
    }

    show() {
        this.canvas.add(this.icon);
    }

    highlightOtherEnd(pointDisplay) {
        this. findOther(pointDisplay).secondaryHighlight();
        this.highLight();
    }

    unHighlightOtherEnd(pointDisplay) {
        this.findOther(pointDisplay).secondaryUnHighlight();
        this.unHighlight();
    }

    findOther(pointDisplay) {
        return (pointDisplay === this.point1) ? this.point2 : this.point1;
    }

    highLight() {
        // this.icon.set("strokeWidth", 1);
    }

    unHighlight() {
        // this.icon.set("strokeWidth", 2);
    }

    moved() {
        const newCoordinates = {
            x1: this.point1.icon.left,
            y1: this.point1.icon.top,
            x2: this.point2.icon.left,
            y2: this.point2.icon.top
        };
        this.icon.set(newCoordinates);
        this.icon.setCoords();
    }

}