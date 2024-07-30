import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";
import {WebsiteImage} from "../WebsiteImage";


export const WebsiteIceMineSweeper = () => {

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
                        <WebsiteImage path="/img/website/players/ice-minesweeper.png"
                                      alt="A picture of a minesweeper ICE puzzle"/><br/>
                        <br/>
                        Minesweeper ICE is the classic minesweeper game
                        (<a href="https://en.wikipedia.org/wiki/Minesweeper_(video_game)">wikipedia article</a>).<br/>
                        <br/>
                        Minesweeper speedrunning is a popular sport, and the world record is under 30 seconds for the expert level. There are common patterns
                        to learn that will greatly speed up the time to solve this puzzle.<br/>
                        <br/>
                        This version of minesweeper has guaranteed safe corners. You can always click these to get a start.<br/>
                        <br/>
                        <br/>
                        Thematically you are fixing corrupted memory of the ICE. Mines are represented as corrupt memory blocks. If you click on a
                        corrupted memory block, you are blocked from making further moves. Your fellow hackers can still continue solving the puzzle.<br/>
                        <br/>
                        You can reset the puzzle at any time by pressing the Reset button. You will need to keep pressing the button for 8 seconds.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/players/ice-minesweeper-blocked.png"
                                      alt="A picture of a minesweeper ICE puzzle with an exploded mine and a clear reset button"/><br/>
                    </p>
                    <h3 className="muted">ICE strength</h3>
                    <p className="text">
                        <ul>
                            <li>Weak ICE corresponds to minesweeper level: beginner<br/><br/></li>
                            <li>Average ICE corresponds to minesweeper level: intermediate<br/><br/></li>
                            <li>Very Strong ICE corresponds to minesweeper level: expert<br/><br/></li>
                            <li>Onyx ICE is a 32 x 18 board with 120 mines<br/><br/></li>
                        </ul>
                    </p>
                    <h3 className="muted">Multiplayer</h3>
                    <p className="text">
                        Multiple hackers can work on the same puzzle at the same time. Minesweeper has some inherent luck, sometimes there is not guaranteed
                        safe move and you have to guess where a mine is. This can be mitigated by playing with multiple players, so if one player is blocked,
                        the other players can continue solving the puzzle.<br/>
                    </p>
                </div>
            </div>
        </div>
    )
}
