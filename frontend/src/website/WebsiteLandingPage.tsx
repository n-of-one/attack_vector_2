import React from "react";
import {Banner} from "../login/Banner";
import {WebsiteImage} from "./WebsiteImage";

/* eslint react/jsx-no-target-blank  : 0*/

export const WebsiteLandingPage = () => {

    return (<div className="container" data-bs-theme="dark">
            <Banner image={true}/>
            <div className="row">
                <div className="col-12">
                    <h3 className="muted">Overview</h3>
                    <p className="text">
                        Attack Vector is an online system designed to make players feel like hackers in a sci-fi world.<br/>
                        <br/>
                        Players 'hack' sites and break 'ICE' by solving puzzle mini-games, to uncover clues put there by the game masters or organizers.
                        Emphasis is on the multiplayer aspect, players are stimulated to work as a team, not as individuals. <br/>
                        <br/>
                        Players will need a laptop and a browser (Chrome, Firefox, Safari) to play the game, it will not work with mobile phones.<br/>
                        <br/>
                        There is a short video that showcases aspects of Attack Vector on <a href="https://www.youtube.com/watch?v=KOkPB3ZrHqM"
                                                                                             target="_blank">YouTube</a>.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/av2-tutorial-site.png" width="400px"/><br/>
                        <i>3 players exploring the tutorial site.</i><br/>

                    </p>
                    <h5 className="muted">Player</h5>
                    <p className="text">Take on the role of a hacker, no real-world hacking experience or programming experience needed.
                        Learn how to play <a href="/website/players">here</a>.<br/>
                        <br/>
                        If you want to experience Attack Vector, go to demo site: <a href="https://attackvector.nl" target="_blank">attackvector.nl</a>. You can log in with a Google
                        account, no registration needed. No personal information is collected.<br/>
                    </p>
                    <h5 className="muted">Game master</h5>
                    <p className="text">Create the sites that players can hack, or learn how to use pre-fab sites during a busy event to generate content on
                        the spot. More information <a href="/website/gms">here</a>.</p>
                    <h5 className="muted">Organizer</h5>
                    <p className="text">Get more information here on hosting Attack Vector and the help that is offered for this. More info <a
                        href="/website/organizers">here</a>.
                    </p>
                    <h5 className="muted">Open source</h5>
                    <p className="text">Attack Vector is open source and free: <a href="https://github.com/n-of-one/attack_vector_2"
                                                                                  target="_blank">https://github.com/n-of-one/attack_vector_2</a>.<br/>
                        It's built using many other open source resources, see: <a href="/about">about</a>.
                    </p>
                </div>
            </div>
        </div>
    )
}
