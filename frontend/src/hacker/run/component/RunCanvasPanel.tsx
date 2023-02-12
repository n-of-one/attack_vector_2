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
                    <HackerImage type="BEAR" fileName="animal-bear4-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="BIRD_1" fileName="animal-bird2.png" onLoad={onLoad}/>
                    <HackerImage type="CAT" fileName="animal-cat3.png" onLoad={onLoad}/>
                    <HackerImage type="CRAB" fileName="animal-crab2.png" onLoad={onLoad}/>
                    <HackerImage type="DINO_1" fileName="animal-dinosaur3.png" onLoad={onLoad}/>
                    <HackerImage type="DINO_2" fileName="animal-dinosaur4.png" onLoad={onLoad}/>
                    <HackerImage type="DRAGON_1" fileName="animal-dragon1.png" onLoad={onLoad}/>
                    <HackerImage type="LION" fileName="animal-lion1-sc36.png" onLoad={onLoad}/>
                    <HackerImage type="GECKO" fileName="animal-lizard2-sc37.png" onLoad={onLoad}/>
                    <HackerImage type="LIZARD" fileName="animal-lizard1.png" onLoad={onLoad}/>
                    <HackerImage type="LIONESS" fileName="animal-lion3-sc37.png" onLoad={onLoad}/>
                    <HackerImage type="MONKEY" fileName="animal-monkey.png" onLoad={onLoad}/>
                    <HackerImage type="COBRA" fileName="animal-snake1.png" onLoad={onLoad}/>
                    <HackerImage type="LOBSTER_1" fileName="animal-lobster.png" onLoad={onLoad}/>
                    <HackerImage type="SHARK" fileName="animal-fish7-sc37.png" onLoad={onLoad}/>
                    <HackerImage type="STINGRAY" fileName="animal-fish6.png" onLoad={onLoad}/>
                    <HackerImage type="FROG" fileName="animal-frog.png" onLoad={onLoad}/>
                    <HackerImage type="BULL" fileName="animal-bull1-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="CROCODILE" fileName="animal-crocodile-sc43.png" onLoad={onLoad}/>
                    <HackerImage type="DOG" fileName="animal-dog5-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="DRAGON_2" fileName="animal-dragon5-sc28.png" onLoad={onLoad}/>
                    <HackerImage type="FISH_1" fileName="animal-fish.png" onLoad={onLoad}/>
                    <HackerImage type="HIPPO" fileName="animal-hippo3-sc22.png" onLoad={onLoad}/>
                    <HackerImage type="HORSE" fileName="animal-horse1.png" onLoad={onLoad}/>
                    <HackerImage type="KOALA" fileName="animal-koala-bear.png" onLoad={onLoad}/>
                    <HackerImage type="SEAHORSE" fileName="animal-seahorse2-sc37.png" onLoad={onLoad}/>
                    <HackerImage type="SNAKE_2" fileName="animal-snake.png" onLoad={onLoad}/>
                    <HackerImage type="UNICORN" fileName="animal-unicorn.png" onLoad={onLoad}/>
                    <HackerImage type="WOLF" fileName="animal-wolf-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="TURTLE" fileName="animal-turtle.png" onLoad={onLoad}/>
                    <HackerImage type="MOOSE" fileName="animal-moose-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="CAMEL" fileName="animal-camel2-sc36.png" onLoad={onLoad}/>
                    <HackerImage type="EAGLE" fileName="animal-bird4-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="DINO_3" fileName="animal-dinosaur1.png" onLoad={onLoad}/>
                    <HackerImage type="DRAGON_3" fileName="animal-dragon2.png" onLoad={onLoad}/>
                    <HackerImage type="ELEPHANT" fileName="animal-elephant1.png" onLoad={onLoad}/>
                    <HackerImage type="FISH_2" fileName="animal-fish13.png" onLoad={onLoad}/>
                    <HackerImage type="LOBSTER_2" fileName="animal-lobster1-sc44.png" onLoad={onLoad}/>
                    <HackerImage type="CAT_2" fileName="animal-cat1.png" onLoad={onLoad}/>
                    <HackerImage type="BIRD_2" fileName="animal-bird.png" onLoad={onLoad}/>
                    <HackerImage type="NOT" fileName="animal-cat-print.png" onLoad={onLoad}/>

                    <PatrollerImage type="PATROLLER_3" fileName="patroller_3.png" onLoad={onLoad}/>


                    <ScanProbeImages />
                </div>
            </div>
        );
    }
}
