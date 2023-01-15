import React, {Component} from 'react';
import runCanvas from "./RunCanvas";
import {
    DATA_STORE, MANUAL_1, MANUAL_2, MANUAL_3, PASSCODE_STORE, RESOURCE_STORE, SYSCON,
    ICE_1, ICE_2, ICE_3, TRANSIT_1, TRANSIT_2, TRANSIT_3, TRANSIT_4, UNHACKABLE
} from "../../../common/enums/NodeTypesNames";
import ScanNodeImage from "./RunNodeImage";
import HackerImage from "../../../common/component/HackerImage";
import ScanProbeImages from "./ScanProbeImages";
import PatrollerImage from "../../../common/component/PatrollerImage";
import {connect} from "react-redux";

class RunCanvasPanel extends Component {

    componentWillMount() {
        // dispatch some actions if you use Redux
    }

    componentDidMount() {
        runCanvas.init(this.props.userId, this.props.dispatch);
    }

    allowDrop(event) {
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

let mapStateToProps = (state) => {
    return {
        userId: state.userId
    };
};

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(RunCanvasPanel);
