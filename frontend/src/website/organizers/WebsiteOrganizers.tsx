import React from "react";
import {Banner} from "../../login/Banner";
import {SilentLink} from "../../common/component/SilentLink";
import {WebsiteImage} from "../WebsiteImage";

/* eslint react/jsx-no-target-blank  : 0*/

export const WebsiteOrganizers = () => {

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
                    <h3 className="muted">Goal</h3>
                    <p className="text">
                        Version 2 of Attack Vector is meant to be as low effort for organizations to use as possible.<br/>
                        <br/>
                        It's been developed for the Dutch Larp <a href="https://eosfrontier.space/" target="_blank">Eos Frontier</a>, but it can be used in any
                        sci-fi event that favors player involvement over realism.<br/>
                        <br/>
                        <WebsiteImage path="/img/website/organizers/av_on_frontier.png" width="800px"/><br/>
                        <i>Players cooperating to solve a Netwalk ICE puzzle on Frontier 17. Photo courtesy of <a href="https://www.hephaestus-aperture.com"
                                                                                                                  target="_blank">
                            Hephaestus Aperture</a>.</i><br/>
                        <br/>
                        Attack Vector is <a href="https://github.com/n-of-one/attack_vector_2" target="_blank">open source</a> under the MIT license, meaning
                        you can use
                        it in most ways you want, including commercially. I (Erik, the creator) encourage it to be used on Live Action Roleplaying events.<br/>
                    </p>
                    <h3 className="muted">Hosting Attack Vector</h3>
                    <p className="text">
                        Attack Vector is free, but it's also a piece of software that needs to be hosted somewhere.<br/>
                        <br/>
                        Fortunately, it's quite cheap to host in the cloud, it runs on an Amazon AWS t4g.micro, costing around 1 dollar-cent/euro-cent per hour
                        of usage.
                        There are additional costs for having an IP address and having a domain name. But it's still so cheap that I am willing to set it up and
                        host it
                        for the first event to allow organisations to see if Attack Vector is something for them.<br/>
                        <br/>
                        If you want to host it yourself, there are instructions in the source code. It requires Java and Mongodb.
                    </p>
                    <h3 className="muted">Discord server - Support</h3>
                    <p className="text">
                        There is a Discord server dedicated to using Attack Vector on your event.<br/>
                        {/* eslint-disable-next-line jsx-a11y/anchor-is-valid */}
                        Invite link: <a href="#" onClick={() => {
                        alert("To discourage spam-bots from entering the Discord server, please copy-paste the link in the address bar " +
                            "and remove the spaces.")
                        return false
                    }}>discord.gg / EkNFsHMbfC</a><br/>
                        <br/>
                        <br/>If you consider using Attack Vector, have questions or problems using it, please go here and ask for help. I usually reply within a
                        day.
                    </p>

                    <h3 className="muted">Customization for a specific event</h3>
                    <p className="text">
                        Attack Vector 2 can be changed to fit the aesthetic of a specific event, but this will require some development. I am happy to
                        perform
                        some simple changes as long as the organization provides the graphics and cooperates in this process.<br/>
                        <br/>
                        A bigger effort is to connect the login of Attack Vector to the login of the event's website. I will help with this depending on the
                        effort
                        involved.
                    </p>
                    <h3 className="muted">Privacy</h3>
                    <p className="text">
                        Attack Vector aims to be as privacy friendly as possible. It does not use cookies to track users, it does not store any personal
                        information other
                        than the information stored in there by the game masters.<br/>
                        <br/>
                        Google Authentication is the default way of logging in so that the system does not know any real usernames or passwords. The system
                        does
                        not store
                        any personal information returned by Google like the name or email address of the person logging in.
                        See the <a href="/privacy" target="_blank">privacy statement</a> for details.<br/>
                        <br/>

                    </p>
                    <h3 className="muted">Development and future features</h3>
                    <p className="text">
                        Version 1 of Attack Vector was first used on an event in 2016. This version did not support multiplayer like version 2. However,
                        over
                        the years a
                        lot of features
                        were added that are currently missing from version 2, see below.<br/>
                        <br/>
                        Version 2 of Attack Vector was first used on an event in 2023.<br/>
                        <br/>
                        Features that will be implemented in the future:<br/>
                        <br/>
                        <ul>
                            <li>Creation of site-templates<br/><br/>
                                One of the major challenges during an event, is that sometimes players come up with some system they want to hack, that the
                                game-masters
                                did not anticipate. To solve this, version 1 allowed the game-masters to create a number of site-templates in advance, but
                                without any
                                specific information in them.<br/><br/>
                                During the event, they could use a very simple web-form to create a site based on a template, and fill in the information
                                that
                                the players
                                would find inside.<br/>
                                <br/>
                                This way, even game masters who don't understand the specifics of how to create good sites, could still provide a site for
                                the
                                players to
                                hack.<br/><br/><br/>
                            </li>
                            <li>Discord integration<br/><br/>
                                Version 1 had a Discord bot that would send updates to a Discord channel. This way, game masters could see the outcome of a
                                hack
                                on their
                                phone.<br/><br/><br/>
                            </li>
                        </ul>
                    </p>
                </div>
            </div>
        </div>
    )
}
