import React from "react";
import {Banner} from "../../../login/Banner";
import {SilentLink} from "../../../common/component/SilentLink";
import {WebsiteImage} from "../../WebsiteImage";


export const WebsiteIceNetwalk = () => {

    return (<div className="container" data-bs-theme="dark">
            <Banner image={true}/>
            <div className="row">
                <div className="col-12 text">
                    <SilentLink href="/website/players"><>&lt; back</>
                    </SilentLink><br/>
                </div>
            </div>

            <div className="row">
                <div className="col-12">
                    <h3 className="muted">Netwalk ICE</h3>
                    <p className="text">
                        <WebsiteImage path="/img/website/players/ice-netwalk.png" alt="A picture of a netalk ICE puzzle"/><br/>
                        <br/>
                        Netwalk ICE asks players to connect all elements to the center. Inspired by <a href="https://www.chiark.greenend.org.uk/~sgtatham/puzzles/js/net.html">Simon Tatham's Net puzzle</a>.<br/>
                        <br/>
                        Click on an element to rotate it.
                        <br/>
                    </p>
                    <h3 className="muted">ICE strength</h3>
                    <p className="text">
                        If the ICE strength is weak, then there are fewer elements.<br/>
                        <br/>
                        If the ICE strength is strong, then there are more elements. If the strength is very strong or greater then the connections are wrapping around the edges of the grid.
                    </p>
                    <h3 className="muted">Multiplayer</h3>
                    <p className="text">
                        Multiple hackers can work on the same puzzle at the same time. Coordination problems are smaller than with Tangle ICE,
                        as each player can just start in a corner and work towards the center.
                    </p>
                </div>
            </div>
        </div>
    )
}
