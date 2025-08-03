import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";
import {WebsiteImage} from "../WebsiteImage";

/* eslint-disable react/jsx-no-target-blank */

export const WebsitePlayerScripts = () => {

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
                    <h3 className="muted">Scripts</h3>
                    <p className="text">
                        Scripts are one-time-use programs that assist you during hacking runs. They can perform a variety of tasks, such as extending your time
                        to hack or helping you break through ICE<br/>
                        <br/>
                        Thematically scripts exploit security flaws. Those flaws will be patched automatically by security systems after use,
                        so all script can only be used once. The flaws are also patched daily at 06:00, so scripts can only be used on the day they are created.<br/>
                        <br/>
                        Check out this video for a player-focused explanation of how scripts work: <a href="https://youtu.be/ri3hewshbKA"
                                                                                                      target="_blank">Youtube</a>.
                    </p>

                    <h5 className="muted">Script types</h5>
                    <p className="text">
                        The specific scripts available in your LARP are determined by your GM. Each script may have one or more effects. When you receive a
                        script, you can hover over its effects to see what they do.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/players/mask-script.png" width="800px"/>
                        <br/>
                    </p>


                    <h5 className="muted">RAM</h5>
                    <p className="text">
                        To use scripts, you need the Scripts skill. This skill gives you a number of RAM blocks, which determine how many scripts you can load
                        at once.<br/>
                        <br/>
                        Each script has a RAM cost. You can only load as many scripts as will fit in your available RAM. After a script is used, its RAM blocks
                        refresh gradually, one by one.<br/>
                        <br/>
                    </p>

                    <h5 className="muted">Access to scripts</h5>
                    <p className="text">
                        How you obtain scripts depends on your LARP. Possible ways include:<br/><br/>
                        <ul>
                            <li>Scripts granted by the GM</li>
                            <li>A daily script allowance</li>
                            <li>Script codes that you can find/buy in the Larp world. You can download scripts using these codes.</li>
                        </ul>
                        You can also share offer your scripts for download by other hackers and give them the codes of your scripts. But be aware: when someone downloads your
                        script, you lose it.
                    </p>

                    <h5 className="muted">Running a script</h5>
                    <p className="text">
                        During a hacking run, you can execute a script with the run command. For example:<br/>
                        <br/>
                        ⇀ run <span className="t_pri">ab55-44ff</span><br/>
                        <br/>
                        <br/>
                        If a script requires extra parameters, it will prompt you accordingly.<br/>
                        <br/>

                        To view your available scripts during a run, click this icon <WebsiteImage path="/img/website/players/script-access-icon.png"
                                                                                                   width="30px"/>&nbsp;at
                        the top-right corner of your map. If you don’t see this icon, you don't have the Scripts skill, have no RAM and cannot run any scripts.
                        <br/>
                    </p>
                </div>
            </div>
        </div>
    )
}
