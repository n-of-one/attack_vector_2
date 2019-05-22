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

const calcLine = (from, to, delta) => {
    const fromOffset = from.size();
    const toOffset = to.size();

    return calcLineWithOffset(from, to, fromOffset, toOffset, delta);
};

const calcLineWithOffset = (from, to, fromOffset, toOffset, delta) => {
    const {xSpan, ySpan, distance} = calcDistance(from, to);

    const [xDelta, yDelta] = determineDelta(xSpan, ySpan, delta);

    const startRatio = fromOffset / distance;
    const finishRatio = (distance - toOffset) / distance;

    const x1 = Math.floor(from.x + xSpan * startRatio) + xDelta;
    const y1 = Math.floor(from.y + ySpan * startRatio) + yDelta;
    const x2 = Math.floor(from.x + xSpan * finishRatio) + xDelta;
    const y2 = Math.floor(from.y + ySpan * finishRatio) + yDelta;

    return new LinePositions(x1, y1, x2, y2);
};

const determineDelta = (xSpan, ySpan, delta) => {
    if (!delta) {
        return [0, 0];
    }

    const dy = ySpan / ((xSpan === 0) ? 1 : xSpan);
    const sign = Math.sign(dy);
    const dyAbs = Math.abs(dy);
    if (dyAbs < 0.5) {
        return [0, delta];
    }
    else if (dyAbs > 5) {
        return [delta, 0];
    }
    else {
        if (sign === 1) {
            return [-delta, delta];
        }
        else {
            return [delta, delta];
        }
    }
};

const calcLineStart = (from, to, fromOffset, delta) => {
    const {distance} = calcDistance(from, to);
    const toOffset = distance - fromOffset;
    return calcLineWithOffset(from, to, fromOffset, toOffset, delta);
};

function easeLinear (t, b, c, d) {
    return b + (t/d) * c;
}



export {animate, calcLine, calcLineStart, easeLinear};