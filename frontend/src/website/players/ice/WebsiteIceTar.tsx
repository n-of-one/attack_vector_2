import React from "react";
import {Banner} from "../../../login/Banner";
import {SilentLink} from "../../../common/component/SilentLink";
import {WebsiteImage} from "../../WebsiteImage";


export const WebsiteIceTar = () => {

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
                    <h3 className="muted">Tar ICE</h3>
                    <p className="text">
                        <WebsiteImage path="/img/website/players/ice-tar.png" alt="A picture of tar ICE puzzle"/><br/>
                        <br/>
                        Tar ICE has no mini-game. It just takes a fixed number of seconds to hack this ICE, this is done automatically, the hacker does not need to do
                        anything.<br/>
                        <br/>
                        If more hackers join, then they each contribute, reducing the time it takes to hack this ICE.<br/>
                        <br/>
                    </p>
                    <h3 className="muted">ICE strength and multiplayer</h3>
                    <p className="text">
                        The purpose of this ICE depends on it's strength.<br/>
                        <br/>
                        Weaker Tar ICE can be overcome in relative short time if multiple hackers join effort. If there is a countdown-timer that requires
                        the tar ICE to be hacked within a certain time, you need to have multiple hackers to make it in time.<br/>
                        <br/>
                        Stronger Tar ICE is meant to deter hackers from trying to hack it. A good site has ways around a Tar ICE. One such option is to find the password
                        to this ICE elsewhere in the site, and use the <a href="/website/players-commands">open</a> command to enter the password to disable
                        this ICE.
                    </p>
                </div>
            </div>
        </div>
    )
}
