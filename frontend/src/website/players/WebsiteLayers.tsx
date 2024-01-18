import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";

export const WebsiteLayers = () => {

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
                    <h3 className="muted">Layer types</h3>
                    <p className="text">
                        These are the main types of layers:<br/>
                        <br/>
                        <ul>
                            <li><span className="terminal_input">OS</span> - Layer 0 is the OS. It's there for flavor, you can't hack it.<br/><br/></li>
                            <li><span className="terminal_input">ICE</span> - Various ICE layers exist to secure underlying layers and prevent movement to other
                                nodes.<br/><br/></li>
                            <li><span className="terminal_input">tripwire</span> - Tripwire layers start a countdown timer when a hacker arrives in the node.<br/><br/>
                                When the timer reaches zero, all hackers are kickeded out of the site, and all ICE puzzles are reset. They have to be hacked again with a
                                new puzzle if they were already hacked. Tripwire timers can be stopped by hacking the right <span
                                    className="terminal_input">core</span> layer.<br/><br/>
                            </li>
                            <li><span className="terminal_input">core</span> - Hacking a core can reset a tripwire timer. It optionally reveals the entire site
                                map.<br/><br/></li>
                            <li><span className="terminal_input">keystore</span> - Contains a password for an ICE layer. The password of a keystore will change if the
                                site is reset. The 'Password' ICE layer has a unchanging password,
                                that is not found in a keystore, but somewhere else.<br/><br/>
                            </li>
                            <li><span className="terminal_input">switch</span> - Switches can have different names, but they all allow the hacker to flip a switch,
                                changing something in the real world. For instance unlocking a door or turning off the life-support.<br/><br/></li>
                            <li><span className="terminal_input">text</span> - There are infinitely many layers that can be created by the game masters. Hacking such a
                                layer will just display some text. This is either a clue or a password, or some other information.<br/><br/></li>

                        </ul>
                    </p>
                </div>
            </div>
        </div>
    )
}
