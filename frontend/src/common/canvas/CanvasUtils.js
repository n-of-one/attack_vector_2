import {fabric} from "fabric";

const animate = (canvas, toAnimate, attribute, value, duration, easing) => {

    const easingFunction = (easing) ? easing : fabric.util.ease.easeInOutSine;
    if (attribute) {
        toAnimate.animate(attribute, value, {
            onChange: canvas.renderAll.bind(canvas),
            duration: duration * 50,
            easing: easingFunction
        });
    }
    else {
        toAnimate.animate(value, {
            onChange: canvas.renderAll.bind(canvas),
            duration: duration * 50,
            easing: easingFunction
        });
    }
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

const calcLine = (from, to, padding) => {
    const fromOffset = from.size();
    const toOffset = to.size();

    return calcLineWithOffset(from, to, fromOffset, toOffset, padding);
};

const calcLineWithOffset = (from, to, fromOffset, toOffset, padding) => {
    const {xSpan, ySpan, distance} = calcDistance(from, to);

    const [xPadding, yPadding] = expandPadding(xSpan, ySpan, padding);

    const startRatio = fromOffset / distance;
    const finishRatio = (distance - toOffset) / distance;

    const x1 = Math.floor(from.x + xSpan * startRatio) + xPadding;
    const y1 = Math.floor(from.y + ySpan * startRatio) + yPadding;
    const x2 = Math.floor(from.x + xSpan * finishRatio) + xPadding;
    const y2 = Math.floor(from.y + ySpan * finishRatio) + yPadding;

    return new LinePositions(x1, y1, x2, y2);
};

const expandPadding = (xSpan, ySpan, padding) => {
    if (!padding) {
        return [0, 0];
    }

    const dy = ySpan / ((xSpan === 0) ? 1 : xSpan);
    const sign = Math.sign(dy);
    const dyAbs = Math.abs(dy);
    if (dyAbs < 0.5) {
        return [0, padding];
    }
    else if (dyAbs > 5) {
        return [padding, 0];
    }
    else {
        if (sign === 1) {
            return [-padding, padding];
        }
        else {
            return [padding, padding];
        }
    }
};

const calcLineStart = (from, to, fromOffset, padding) => {
    const {distance} = calcDistance(from, to);
    const toOffset = distance - fromOffset;
    return calcLineWithOffset(from, to, fromOffset, toOffset, padding);
};

function easeLinear (t, b, c, d) {
    return b + (t/d) * c;
}



export {animate, calcLine, calcLineStart, easeLinear};