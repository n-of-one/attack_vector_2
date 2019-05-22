import React from 'react';
import {connect} from "react-redux";
import {DRAG_DROP_START} from "../EditorActions";

/* eslint jsx-a11y/alt-text: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
    }
};
let mapStateToProps = (state) => {
    return {
        theme: state.theme
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({dispatch, theme, type, ice, title, onLoad}) => {

        let dragStart = (syntheticEvent) => {
            let event = syntheticEvent.nativeEvent;
            let x = event.offsetX;
            let y = event.offsetY;
            let halfWidth = event.target.width / 2;
            let halfHeight = event.target.height / 2;

            let dx = x - halfWidth;
            let dy = y - halfHeight;

            dispatch({type: DRAG_DROP_START, data: {type: type, dx: dx, dy: dy, ice: ice}});
        };

        const dirAndName = type.dir + "/" + type.name + ".png";

        const root = "/img/" + theme + "/nodes/run/";
        const pathRegular =  root + "free/" + dirAndName;
        const pathIce =  root + "protected/" + dirAndName;

        const idFree = type.name + "_FREE";
        const idProtected = type.name + "_PROTECTED";

        if (ice) {
            return (
            <span>
                <img style={{display: 'none'}} src={pathRegular} height="80" width="80" id={idFree} />
                <img style={{display: 'inline'}} src={pathIce} height="80" width="80" id={idProtected} title={title} onDragStart={ (event) => dragStart(event) }
                     onLoad={() => onLoad()}/>
            </span>
            )


        }
        else {
            return (
                <span>
                <img style={{display: 'inline'}} src={pathRegular} height="80" width="80" id={idFree} title={title} onDragStart={ (event) => dragStart(event) }
                     onLoad={() => onLoad()} />
                <img style={{display: 'none'}} src={pathIce} height="80" width="80" id={idProtected} />
            </span>
            );

        }

    });