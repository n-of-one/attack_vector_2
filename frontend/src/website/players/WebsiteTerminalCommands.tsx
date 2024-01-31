import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";

export const WebsiteTerminalCommands = () => {
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
                <h3 className="muted">Approaching the site</h3>
                <p className="text">
                    When you approach the site, you see only the first node. You can choose to either scan the site, or attack it.<br/>
                    <br/>
                    <ul>
                        <li><span className="terminal_input">help</span> - get a list of available commands</li>
                        <li><span className="terminal_input">scan</span> - scan the site to reveal the unprotected part of its network.<br/><br/>
                            This is usually the first thing you do when you arrive at a new site to show the parts of the site that are not protected by ICE.<br/><br/>
                        </li>
                        <li><span className="terminal_input">attack</span> - enter the site and start the attack</li>
                    </ul>
                </p>

                <h3 className="muted">Inside the site</h3>
                <p className="text">
                    After you have entered the attack command, you have entered the site. You can now move around and interact with layers.<br/>
                    <br/>
                    <ul>
                        <li><span className="terminal_input">help</span> - get a list of available commands.</li>
                        <li><span className="terminal_input">view</span> - show layers in this service. Alternative to clicking on the node on the map.</li>
                        <li><span className="terminal_input">move</span> - move to another node. For example: <span className="terminal_input">move <span
                            className="terminal_style_ok">01</span></span><br/><br/></li>
                        <li><span className="terminal_input">hack</span> - hack a layer. For example: <span className="terminal_input">hack <span
                            className="terminal_style_primary">2</span></span><br/><br/>
                            For switch layers, this will open the switch interface.<br/>
                            For other non-ICE layers, this will reveal information.<br/>
                            For ICE layers, this will start the puzzle mini-game.<br/><br/>
                        </li>
                        <li><span className="terminal_input">password</span> - open the password UI for ICE. For example: <span className="terminal_input">password <span
                            className="terminal_style_primary">2</span></span><br/><br/>
                            If you have found the password for this ICE, you can enter it here. Allows you to bypass the mini-game.<br/><br/>
                        </li>
                        <li><span className="terminal_input">scan</span> - do a new scan after ICE has been hacked, to reveal new parts of the site.</li>
                        <li><span className="terminal_input">dc</span> - hack a layer. Disconnect from the site.</li>
                    </ul>
                </p>
                <h3 className="muted">Social commands</h3>
                <p className="text">
                    These can be given either outside or inside a site.<br/>
                    <br/>
                    <ul>
                        <li><span className="terminal_input">/share</span> - share your run with another player, allowing them to join you and cooperate. For example: <span
                            className="terminal_input terminal_style_warn">/share <span className="terminal_style_info">paradox</span></span><br/><br/>
                            You can also share the site name with another player. They can use this to enter the site, but they would be in their own 'run'. This means
                            that you cannot see each other, or collaborate. They are still hacking the same site, so any ICE hacking in one run will affect all runs.<br/><br/>
                        </li>
                    </ul>
                </p>

            </div>
        </div>
    </div>)
}

