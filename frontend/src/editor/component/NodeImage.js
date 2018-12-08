import React from 'react';
import {connect} from "react-redux";
import {DRAG_DROP_START} from "../EditorActions";
import canvasMap from "../component/canvas/CanvasMap";

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
    ({dispatch, theme, type, ice, title}) => {

        let onLoad = () => {
            canvasMap.render();
        };

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

        let nameRegular = type + "_REGULAR";
        let nameIce = type + "_ICE";
        let pathRegular = "/img/nodes/" + theme + "/" + nameRegular + ".png";
        let pathIce = "/img/nodes/" + theme + "/" + nameIce + ".png";

        if (ice) {
            return (
            <span>
                <img style={{display: 'none'}} src={pathRegular} height="80" width="80" name={nameRegular} />
                <img style={{display: 'inline'}} src={pathIce} height="80" width="80" name={nameIce} title={title} onDragStart={ (event) => dragStart(event) }
                     onLoad={() => onLoad()}/>
            </span>
            )


        }
        else {
            return (
                <span>
                <img style={{display: 'inline'}} src={pathRegular} height="80" width="80" name={nameRegular} title={title} onDragStart={ (event) => dragStart(event) }
                     onLoad={() => onLoad()} />
                <img style={{display: 'none'}} src={pathIce} height="80" width="80" name={nameIce} />
            </span>
            );

        }

    });