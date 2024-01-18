import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";
import {WebsiteImage} from "../WebsiteImage";


export const WebsitePlayers = () => {

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
                <h3 className="muted">Sites</h3>
                <p className="text">
                    As a player you are a hacker that can infiltrate the computer systems of the game world.
                    These systems are called "sites" in Attack Vector.
                    Once you know the name of a site, you can hack it. You find the site names in the game world, or receive them from a game master.<br/>
                    <br/>
                    A site consists of nodes that are connected to each other. The general goal is to progress deeper into a site and find information hidden in
                    nodes<br/>
                    <br/>
                    <WebsiteImage path="/img/website/players/sitemap.png" alt="A picture of a site map"/><br/>
                </p>

                <h3 className="muted">Nodes and layers</h3>
                <p className="text">
                    Here you see a single node and the layers inside. Layer <span className="text-primary">3</span> is an HR database that can be hacked to find
                    information about employees.<br/>
                    <br/>
                    <WebsiteImage path="/img/website/players/layers.png" alt="A picture of layers in a node"/><br/>
                    <br/>
                    Layer <span className="text-primary">2</span> is an ICE layer that protects layers <span className="text-primary">1</span> and <span
                    className="text-primary">0</span>.<br/>
                    <br/>
                    <a href="/website/players-layers">Layers in the game</a>
                </p>

                <h3 className="muted">Terminal</h3>
                <p className="text">
                    Hacking a site is done by entering the commands in the terminal. There are only a few commands to learn like "move" or "hack". You can always use the
                    command 'help' to see the available commands.<br/>
                    <br/>
                    <a href="/website/players-commands">Terminal commands</a>
                </p>

                <h3 className="muted">ICE Puzzles</h3>
                <p className="text">
                    Hacking ICE is done by solving puzzle mini-games:<br/>
                    <br/>
                </p>
                <ul>
                    <li className="text"><a href="/website/players-ice-word-search">Word search</a><br/><br/></li>
                    <li className="text"><a href="/website/players-ice-tangle">Tangle</a><br/><br/></li>
                    <li className="text"><a href="/website/players-ice-netwalk">Netwalk</a><br/><br/></li>
                    <li className="text"><a href="/website/players-ice-password">Password</a><br/><br/></li>
                    <li className="text"><a href="/website/players-ice-tar">Tar</a></li>
                </ul>
            </div>
        </div>


    </div>)
}

