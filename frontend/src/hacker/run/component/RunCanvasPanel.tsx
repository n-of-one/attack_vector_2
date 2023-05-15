import React, {Component} from 'react';
import {runCanvas} from "./RunCanvas";
import {
    TYPE_DATA_STORE, TYPE_MANUAL_1, TYPE_MANUAL_2, TYPE_MANUAL_3, TYPE_PASSCODE_STORE, TYPE_RESOURCE_STORE, TYPE_SYSCON,
    TYPE_ICE_1, TYPE_ICE_2, TYPE_ICE_3, TYPE_TRANSIT_1, TYPE_TRANSIT_2, TYPE_TRANSIT_3, TYPE_TRANSIT_4, TYPE_UNHACKABLE
} from "../../../common/enums/NodeTypesNames";
import {RunNodeImage} from "./RunNodeImage";
import {HackerImage} from "../../../common/component/HackerImage";
import {ScanProbeImages} from "./ScanProbeImages";
import {PatrollerImage} from "../../../common/component/PatrollerImage";
import {Dispatch} from "redux";

interface Props {
    dispatch: Dispatch,
    userId: string
}

export class RunCanvasPanel extends Component<Props> {

    componentWillMount() {
        // dispatch some actions if you use Redux
    }

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
                <canvas id="runCanvas" width="607" height="815" style={{
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
                    <HackerImage type="BEAR" onLoad={onLoad}/>
                    <HackerImage type="BIRD_1" onLoad={onLoad}/>
                    <HackerImage type="CAT" onLoad={onLoad}/>
                    <HackerImage type="CRAB" onLoad={onLoad}/>
                    <HackerImage type="DINO_1" onLoad={onLoad}/>
                    <HackerImage type="DINO_2" onLoad={onLoad}/>
                    <HackerImage type="DRAGON_1" onLoad={onLoad}/>
                    <HackerImage type="LION" onLoad={onLoad}/>
                    <HackerImage type="GECKO" onLoad={onLoad}/>
                    <HackerImage type="LIZARD" onLoad={onLoad}/>
                    <HackerImage type="LIONESS" onLoad={onLoad}/>
                    <HackerImage type="MONKEY" onLoad={onLoad}/>
                    <HackerImage type="COBRA" onLoad={onLoad}/>
                    <HackerImage type="LOBSTER_1" onLoad={onLoad}/>
                    <HackerImage type="SHARK" onLoad={onLoad}/>
                    <HackerImage type="STINGRAY" onLoad={onLoad}/>
                    <HackerImage type="FROG" onLoad={onLoad}/>
                    <HackerImage type="BULL" onLoad={onLoad}/>
                    <HackerImage type="CROCODILE" onLoad={onLoad}/>
                    <HackerImage type="DOG" onLoad={onLoad}/>
                    <HackerImage type="DRAGON_2" onLoad={onLoad}/>
                    <HackerImage type="FISH_1" onLoad={onLoad}/>
                    <HackerImage type="HIPPO" onLoad={onLoad}/>
                    <HackerImage type="HORSE" onLoad={onLoad}/>
                    <HackerImage type="KOALA" onLoad={onLoad}/>
                    <HackerImage type="SEAHORSE" onLoad={onLoad}/>
                    <HackerImage type="SNAKE_2" onLoad={onLoad}/>
                    <HackerImage type="UNICORN" onLoad={onLoad}/>
                    <HackerImage type="WOLF" onLoad={onLoad}/>
                    <HackerImage type="TURTLE" onLoad={onLoad}/>
                    <HackerImage type="MOOSE" onLoad={onLoad}/>
                    <HackerImage type="CAMEL" onLoad={onLoad}/>
                    <HackerImage type="EAGLE" onLoad={onLoad}/>
                    <HackerImage type="DINO_3" onLoad={onLoad}/>
                    <HackerImage type="DRAGON_3" onLoad={onLoad}/>
                    <HackerImage type="ELEPHANT" onLoad={onLoad}/>
                    <HackerImage type="FISH_2" onLoad={onLoad}/>
                    <HackerImage type="LOBSTER_2" onLoad={onLoad}/>
                    <HackerImage type="CAT_2" onLoad={onLoad}/>
                    <HackerImage type="BIRD_2" onLoad={onLoad}/>
                    <HackerImage type="NOT" onLoad={onLoad}/>

                    <PatrollerImage type="PATROLLER_3" fileName="patroller_3.png" onLoad={onLoad}/>


                    <ScanProbeImages/>
                </div>
            </div>
        );
    }
}
