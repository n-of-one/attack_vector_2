import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {DRAG_DROP_START} from "../../../EditorActions";
import {EditorState} from "../../../EditorRootReducer";
import {NodeFileType} from "../../../../common/enums/NodeTypesNames";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type: NodeFileType,
    onLoad: () => void,
    ice: boolean,
    title: string
}
export const EditorNodeImage = (props: Props) => {

    const dispatch = useDispatch()
    const theme = useSelector((state: EditorState) => state.theme);

    let dragStart = (syntheticEvent: any) => {
        let event = syntheticEvent.nativeEvent;
        let x = event.offsetX;
        let y = event.offsetY;
        let halfWidth = event.target.width / 2;
        let halfHeight = event.target.height / 2;

        let dx = x - halfWidth;
        let dy = y - halfHeight;

        dispatch({type: DRAG_DROP_START, data: {type: props.type, dx: dx, dy: dy, ice: props.ice}});
    };

    const dirAndName = props.type.dir + "/" + props.type.name + ".png";

    const root = "/img/" + theme + "/nodes/run/";
    const pathRegular = root + "free/" + dirAndName;
    const pathIce = root + "protected/" + dirAndName;

    const idFree = props.type.name + "_FREE";
    const idProtected = props.type.name + "_PROTECTED";

    if (props.ice) {
        return (
            <span>
                <img style={{display: 'none'}} src={pathRegular} height="80" width="80" id={idFree}/>
                <img style={{display: 'inline'}} src={pathIce} height="80" width="80" id={idProtected} title={props.title}
                     onDragStart={(event) => dragStart(event)}
                     onLoad={() => props.onLoad()}/>
            </span>
        )


    } else {
        return (
            <span>
                <img style={{display: 'inline'}} src={pathRegular} height="80" width="80" id={idFree} title={props.title}
                     onDragStart={(event) => dragStart(event)}
                     onLoad={() => props.onLoad()}/>
                <img style={{display: 'none'}} src={pathIce} height="80" width="80" id={idProtected}/>
            </span>
        );

    }

}
