import {fabric} from "fabric";

const animate = (canvas, toAnimate, attribute, value, duration, easing) => {
    const easingFunction = (easing) ? easing : fabric.util.ease.easeInOutSine;
    toAnimate.animate(attribute, value, {
        onChange: canvas.renderAll.bind(canvas),
        duration: duration * 50,
        easing: easingFunction
    });
};


export {animate};