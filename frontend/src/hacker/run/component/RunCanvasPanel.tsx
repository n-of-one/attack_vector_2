import React, {Component} from 'react';
import {runCanvas} from "./RunCanvas";
import {
    TYPE_DATA_STORE,
    TYPE_ICE_1,
    TYPE_ICE_2,
    TYPE_ICE_3,
    TYPE_MANUAL_1,
    TYPE_MANUAL_2,
    TYPE_MANUAL_3,
    TYPE_PASSCODE_STORE,
    TYPE_RESOURCE_STORE,
    TYPE_SYSCON,
    TYPE_TRANSIT_1,
    TYPE_TRANSIT_2,
    TYPE_TRANSIT_3,
    TYPE_TRANSIT_4,
    TYPE_UNHACKABLE
} from "../../../common/enums/NodeTypesNames";
import {RunNodeImage} from "./RunNodeImage";
import {HackerImage} from "../../../common/component/HackerImage";
import {ScanProbeImages} from "./ScanProbeImages";
import {Dispatch} from "redux";
import {PatrollerImage} from "../../../common/component/PatrollerImage";
import {HackerIcon} from "../../../common/users/HackerIcon";

interface Props {
    dispatch: Dispatch,
    userId: string
}

export class RunCanvasPanel extends Component<Props> {


    componentDidMount() {
        runCanvas.init(this.props.userId, this.props.dispatch);
    }

    allowDrop(event: any) {
        event.preventDefault();
    }

    render() {
        const onLoad = () => {
            runCanvas.render();
        };

        return (
            <div id="canvas-container" style={{height: "850px"}}>
                <canvas id="runCanvas" width="1250" height="815" style={{
                    "borderRadius": "3px 3px 3px 3px",
                    "marginTop": "10px",
                    "marginBottom": "10px",
                }}/>
                <div className="dontDisplay">
                    <RunNodeImage type={TYPE_SYSCON} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_DATA_STORE} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_PASSCODE_STORE} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_RESOURCE_STORE} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_TRANSIT_1} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_TRANSIT_2} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_TRANSIT_3} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_TRANSIT_4} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_ICE_1} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_ICE_2} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_ICE_3} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_UNHACKABLE} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_MANUAL_1} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_MANUAL_2} onLoad={onLoad}/>
                    <RunNodeImage type={TYPE_MANUAL_3} onLoad={onLoad}/>
                    {
                        Object.keys(HackerIcon).map( icon => {
                            return (
                            <span key={`hacker_icon_${icon}`}>
                                <HackerImage type={icon} you={true} onLoad={onLoad}/>
                                <HackerImage type={icon} you={false} onLoad={onLoad}/>
                            </span>
                        )
                        } )
                    }

                    <PatrollerImage type="PATROLLER_3" fileName="patroller_3.png" onLoad={onLoad}/>


                    <ScanProbeImages/>
                </div>
            </div>
        );
    }
}
