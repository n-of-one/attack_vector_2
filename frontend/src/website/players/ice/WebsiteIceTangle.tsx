import React from "react";
import {Banner} from "../../../login/Banner";
import {SilentLink} from "../../../common/component/SilentLink";
import {WebsiteImage} from "../../WebsiteImage";


export const WebsiteIceTangle = () => {

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
                    <h3 className="muted">Tangle ICE</h3>
                    <p className="text">
                        <WebsiteImage path="/img/website/players/ice-tangle.png" alt="A picture of a tangle ICE puzzle"/><br/>
                        <br/>
                        Tangle ICE asks players to untangle a mess of lines so that none are crossing. Inspired by <a
                        href="https://www.chiark.greenend.org.uk/~sgtatham/puzzles/js/untangle.html">Simon Tatham's Untangle puzzle</a>.<br/>
                        <br/>
                        Drag the connection points around with your mouse.
                        <br/>
                    </p>
                    <h3 className="muted">ICE strength</h3>
                    <p className="text">
                        If the ICE strength is weak, then there are fewer lines to untangle.<br/>
                        <br/>
                        If the ICE strength is strong, then there are many more lines to untangle.
                    </p>
                    <h3 className="muted">Clusters</h3>
                    <p className="text">
                        This ICE can have 2 or more clusters. Each cluster is a separate set of connected lines.<br/>
                        <br/>
                        Clusters can be untangled separately.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/players/ice-tangle-clusters.png" alt="A picture of a tangle ICE puzzle with 2 clusters"/><br/>
                    </p>
                    <h3 className="muted">Multiplayer</h3>
                    <p className="text">
                        Multiple hackers can work on the same puzzle at the same time. This introduces coordination problems, as players can undo each other's
                        work.<br/>
                        <br/>
                        In practice, it's only useful for two or three players to work on the same cluster at the same time. More players will just be in each
                        other's way.<br/>
                        <br/>
                        So, if Tangle ICE has 2 or more clusters, then it becomes more useful for 3+ players to cooperate to solve it.
                    </p>
                </div>
            </div>
        </div>
    )
}
