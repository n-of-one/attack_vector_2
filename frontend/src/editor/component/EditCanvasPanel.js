import React, {Component} from 'react';
import EditorCanvas from "./canvas/EditorCanvas";
import { DRAG_DROP_END } from "../EditorActions"

export default class EditCanvasPanel extends Component {

    componentWillMount() {
        // dispatch some actions if you use Redux
    }

    componentDidMount() {
        EditorCanvas.init(this.props.dispatch);
    }

    allowDrop(event) {
        event.preventDefault();
    }

    drop_image_and_create_node(syntheticEvent) {

        let event = syntheticEvent.nativeEvent;
        let x = event.offsetX;
        let y = event.offsetY;
        this.props.dispatch({type: DRAG_DROP_END, x: x, y: y, dragAndDropState: this.props.dragAndDropState});
        event.preventDefault();
    }

    render() {
        return (
            <div className="col-lg-5" id="canvas-col">
                <span className="text-muted">&nbsp;Site editor</span>
                <br/>
                <div id="canvas-container" onDragOver={(event) => this.allowDrop(event)}
                     onDrop={ (event) => this.drop_image_and_create_node(event)}>
                    <canvas id="canvas" width="607" height="815" style={{"borderRadius": "3px 3px 3px 3px"}}/>
                </div>
            </div>
        );
    }
}
