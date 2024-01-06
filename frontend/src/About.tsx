import React from "react";
import {Banner} from "./login/Banner";


export const About = () => {
    return (<div className="container" data-bs-theme="dark">
        <Banner/>
        <br/>
        <br/>
        <div className="row text">
                <div className="col-12">
                    <h3>About</h3>
                    <p>
                        Attack Vector 2 is a game that simulates hacking. It is designed to be used as part of a Live Action Role Playing (LARP) event.
                    </p>
                    <p>
                        The game aims to do the following:
                    </p>
                    <p>
                        <ul>
                            <li>Make players to feel like hackers, even if they know nothing about real-world hacking</li>
                            <li>Stimulate cooperative play between hackers</li>
                            <li>Provide ways to interact with the non-computer world</li>
                        </ul>
                    </p>
                    <p>
                        The setting is a non-descript science fiction setting. Hacker character hack 'sites' that are protected by 'ICE'. The hacking takes the form of solving
                        puzzles like Word-search, Network jigsaw and Untangle. The sites form meta-puzzles where there can be timers to beat, passwords to find or other higher-level puzzles to solve.</p>
                    <p>Sites are created by Game masters, who have an visual editor and other tools to create and maintain sites for use in-game.</p>
                    <br/>
                    <h3>Privacy policy</h3>
                    <p>
                        Link to the <a href="/privacy" target="_blank">privacy policy</a>.
                    </p>
                    <p>
                        Attack Vector 2 is very privacy conscious and aims to not collect or store any personal information. To that end, no usernames, email addresses or passwords are stored.
                        Login is done via an external system, such as your larp's website or Google. When Google is used, only a one-way hash of the google-id is stored, not the email address, name or profile picture.
                    </p>
                    <br/>
                    <h3>Open source</h3>
                    <p>
                        Attack Vector 2 is open source. The source code is available at <a href="https://github.com/n-of-one/attack_vector_2" target="_blank">https://github.com/n-of-one/attack_vector_2</a>.
                        If you want to know more, have found a <a href="https://github.com/n-of-one/attack_vector_2/issues" target="_blank">bug</a>, or want help to use it in your event, please visit the github page.
                    </p>
                </div>
        </div>
    </div>)

}