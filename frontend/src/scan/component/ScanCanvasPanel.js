import React, {Component} from 'react';
import scanCanvas from "./canvas/ScanCanvas";

export default class ScanCanvasPanel extends Component {

    componentWillMount() {
        // dispatch some actions if you use Redux
    }

    componentDidMount() {
        scanCanvas.init(this.props.dispatch);
    }

    allowDrop(event) {
        event.preventDefault();
    }

    render() {
        return (
            <div id="canvas-container">
                <canvas id="scanCanvas" className="siteMap"/>
            </div>
        );
    }
}
