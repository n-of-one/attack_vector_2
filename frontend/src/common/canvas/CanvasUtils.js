import {fabric} from "fabric";

const animate = (canvas, toAnimate, attribute, value, duration, easing) => {

    const easingFunction = (easing) ? easing : fabric.util.ease.easeInOutSine;
    toAnimate.animate(attribute, value, {
        onChange: canvas.renderAll.bind(canvas),
        duration: duration * 50,
        easing: easingFunction
    });
};


class LinePositions {

    line = null;
    constructor(x1, y1, x2, y2) {
        this.line = [x1, y1, x2, y2];
    }

    asArray() {
        return this.line;
    }

    asCoordinates() {
        return   {
            x1: this.line[0],
            y1: this.line[1],
            x2: this.line[2],
            y2: this.line[3]
        };
    }


}

const calcDistance = (from, to) => {
    const xSpan = to.x - from.x;
    const ySpan = to.y - from.y;
    const distance = Math.sqrt(xSpan * xSpan + ySpan * ySpan);

    return {xSpan: xSpan, ySpan: ySpan, distance: distance};
};

const calcLine = (from, to, fromOffset, toOffset, delta) => {

    let xDelta = 0;
    let yDelta = 0;
    if (delta) {
        const dy = Math.abs(ySpan / (xSpan === 0) ? 1 : xSpan);
        if (dy < 0.5) {
            xDelta = delta;
            yDelta = 0;
        }
        else {
            if (dy > 5) {
                xDelta = 0;
                yDelta = delta;
            }
            else {
                xDelta = delta;
                yDelta = delta;
            }
        }
    }

    const {xSpan, ySpan, distance} = calcDistance(from, to);

    const startRatio = fromOffset / distance;
    const finishRatio = (distance - toOffset) / distance;

    const x1 = Math.floor(from.x + xSpan * startRatio) + xDelta;
    const y1 = Math.floor(from.y + ySpan * startRatio) + yDelta;
    const x2 = Math.floor(from.x + xSpan * finishRatio) + xDelta;
    const y2 = Math.floor(from.y + ySpan * finishRatio) + yDelta;

    return new LinePositions(x1, y1, x2, y2);
};

const calcLineStart = (from, to, fromOffset, delta) => {
    const {distance} = calcDistance(from, to);
    const toOffset = distance - fromOffset;
    return calcLine(from, to, fromOffset, toOffset, delta);
};

function easeLinear (t, b, c, d) {
    return b + (t/d) * c;
}



export {animate, calcLine, calcLineStart, easeLinear};