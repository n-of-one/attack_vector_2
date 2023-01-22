import webSocketConnection from "../../common/WebSocketConnection";
import {DragAndDropState} from "../reducer/DragAndDropReducer";

export const sendSiteDataChanged = (siteId: string, field: string, value: string) => {
    const payload = {siteId, field, value};
    const body = JSON.stringify(payload);
    webSocketConnection.send("/av/editor/editSiteData", body);
}

export const sendAddNode = (eventX: number, eventY: number, dragAndDropState: DragAndDropState, siteId: string) => {
    let x = eventX - dragAndDropState.dx;
    let y = eventY - dragAndDropState.dy;
    let nodeType = dragAndDropState.type.toUpperCase();
    let payload = {siteId, x, y, type: nodeType};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/editor/addNode", body);
}