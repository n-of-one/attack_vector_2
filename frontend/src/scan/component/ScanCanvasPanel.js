import React, {Component} from 'react';
import scanCanvas from "./canvas/ScanCanvas";
import NodeImage from "../../editor/component/NodeImage";
import {
    DATA_STORE,
    MAGIC_EYE, MANUAL_1, MANUAL_2, MANUAL_3,
    PASSCODE_STORE, PASSWORD_GUESS,
    RESOURCE_STORE,
    SYSCON,
    TRANSIT_1,
    TRANSIT_2,
    TRANSIT_3,
    TRANSIT_4, UNHACKABLE,
    WORD_SEARCH
} from "../../common/NodeTypesNames";

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
                <NodeImage type={SYSCON} />
                <NodeImage type={DATA_STORE} />
                <NodeImage type={PASSCODE_STORE} />
                <NodeImage type={RESOURCE_STORE} />
                <NodeImage type={TRANSIT_1} />
                <NodeImage type={TRANSIT_2} />
                <NodeImage type={TRANSIT_3} />
                <NodeImage type={TRANSIT_4} />
                <NodeImage type={WORD_SEARCH} />
                <NodeImage type={MAGIC_EYE} />
                <NodeImage type={PASSWORD_GUESS} />
                <NodeImage type={UNHACKABLE} />
                <NodeImage type={MANUAL_1} ice={true}/>
                <NodeImage type={MANUAL_2} ice={true}/>
                <NodeImage type={MANUAL_3} ice={true}/>
            </div>
        );
    }
}
