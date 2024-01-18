import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";
import {WebsiteImage} from "../WebsiteImage";


export const WebsiteGms = () => {

    return (<div className="container" data-bs-theme="dark">
            <Banner image={true}/>
            <div className="row">
                <div className="col-12 text">
                    <SilentLink href="/website"><>&lt; back</>
                    </SilentLink><br/>
                </div>
            </div>

            <div className="row">
                <div className="col-12">
                    <h3 className="muted">Creating sites</h3>
                    <p className="text">
                        As a game master you create the sites for your players to play with. You do this using a visual editor.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/gm/editor.png" alt="A picture of the site editor"/><br/>
                        <br/>
                        You can use drag and drop to add new nodes, add layers and enter the info that players can find in the site.<br/>
                    </p>
                    <h3 className="muted">Creating complex sites</h3>
                    <p className="text">
                        Creating a simple site with some ICE to hack, and some info to find is easy.<br/>
                        <br/>
                        Creating a more complex site with passwords scattered throughout,
                        countdown timers to beat, and meta puzzles involving other elements from your setting, well that will take some more effort.<br/>
                        <br/>
                        There is the option to export and import sites, so you can collaborate with other game masters or organizers to create sites.<br/>
                        <br/>
                        But ultimately the most fun can be had by tailoring your site to your event and players.
                    </p>
                    <h3 className="muted">Interaction with the real world</h3>
                    <p className="text">
                        There is one layer that is designed to allow interaction with the real world: the switch and status light.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/gm/status-light-red.png" alt="A picture of a status light"/><br/>
                        <br/>
                        The status light can be viewed on a different computer or phone (yes, this is meant for phones, unlike the rest of Attack Vector).<br/>
                        <br/>
                        When the hackers get access to the switch layer, they can flip the switch, and the color status light will change.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/gm/switch.png" alt="A picture of a switch"/><br/>
                        <br/>
                        The status light could be displayed on a phone next to a door, to indicate to the players if the door is locked or unlocked. This way,
                        one group of players 'ín the field' could need the hackers to unlock the door for them.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/gm/status-light-green.png" alt="A picture of a switch"/><br/>
                        <br/>
                        The words on a switch & status light can be changed, so it can also be used disarm a trap, or turn off the life-support of a patient, etc.

                    </p>
                </div>
            </div>
        </div>
    )
}
