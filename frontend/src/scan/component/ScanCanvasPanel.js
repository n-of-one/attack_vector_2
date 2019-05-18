import React, {Component} from 'react';
import scanCanvas from "./canvas/ScanCanvas";
import {
    DATA_STORE, MANUAL_1, MANUAL_2, MANUAL_3, PASSCODE_STORE, RESOURCE_STORE, SYSCON,
    ICE_1, ICE_2, ICE_3, TRANSIT_1, TRANSIT_2, TRANSIT_3, TRANSIT_4, UNHACKABLE
} from "../../common/enums/NodeTypesNames";
import ScanNodeImage from "./ScanNodeImage";
import HackerImage from "../../common/component/HackerImage";
import ScanProbeImages from "./ScanProbeImages";
import connect from "react-redux/es/connect/connect";

class ScanCanvasPanel extends Component {

    componentWillMount() {
        // dispatch some actions if you use Redux
    }

    componentDidMount() {
        scanCanvas.init(this.props.userId, this.props.dispatch);
    }

    allowDrop(event) {
        event.preventDefault();
    }

    render() {
        const onLoad = () => {
            scanCanvas.render();
        };

        return (
            <div id="canvas-container">
                <canvas id="scanCanvas" className="siteMap"/>
                <div className="dontDisplay">
                    <ScanNodeImage type={SYSCON} onLoad={onLoad}/>
                    <ScanNodeImage type={DATA_STORE} onLoad={onLoad}/>
                    <ScanNodeImage type={PASSCODE_STORE} onLoad={onLoad}/>
                    <ScanNodeImage type={RESOURCE_STORE} onLoad={onLoad}/>
                    <ScanNodeImage type={TRANSIT_1} onLoad={onLoad}/>
                    <ScanNodeImage type={TRANSIT_2} onLoad={onLoad}/>
                    <ScanNodeImage type={TRANSIT_3} onLoad={onLoad}/>
                    <ScanNodeImage type={TRANSIT_4} onLoad={onLoad}/>
                    <ScanNodeImage type={ICE_1} onLoad={onLoad}/>
                    <ScanNodeImage type={ICE_2} onLoad={onLoad}/>
                    <ScanNodeImage type={ICE_3} onLoad={onLoad}/>
                    <ScanNodeImage type={UNHACKABLE} onLoad={onLoad}/>
                    <ScanNodeImage type={MANUAL_1} ice={true} onLoad={onLoad}/>
                    <ScanNodeImage type={MANUAL_2} ice={true} onLoad={onLoad}/>
                    <ScanNodeImage type={MANUAL_3} ice={true} onLoad={onLoad}/>
                    <HackerImage type="SCORPION" fileName="astrology1-scorpion-sc37.png" onLoad={onLoad}/>
                    <ScanProbeImages />
                </div>
            </div>
        );
    }
}

let mapStateToProps = (state) => {
    return {
        userId: state.user.id
    };
};

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(ScanCanvasPanel);
