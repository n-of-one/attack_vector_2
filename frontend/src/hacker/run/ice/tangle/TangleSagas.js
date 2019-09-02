import tangleIceManager from "./TangleIceManager";

export function* tangleIceStartHack(action) {
    yield tangleIceManager.startHack(action.data);
}