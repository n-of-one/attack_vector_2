import React from "react";
import {Banner} from "../../../login/Banner";
import {SilentLink} from "../../../common/component/SilentLink";


export const WebsiteSkills = () => {

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
                    <h3 className="muted">Skills</h3>
                    <p className="text">
                        <br/>
                        SKills are abilities that hackers can have that help them in the game. They are optional, not needed for Attack Vector, but could be
                        used to differentiate between professional hackers and hackers who are just starting out, or characters just helping out.
                        <br/>
                    </p>
                    <h3 className="muted">Template</h3>
                    <p className="text">
                        You can define the base skills that all hackers will start out with. Do this by assigning those skills to the user called:
                        "Template". When a new hacker is created, it will receive the same skills as the template user.<br/>
                    </p>

                    <h3 className="muted">List of skills</h3><br/>

                    <h5 className="muted">Scan</h5>
                    <p className="text">
                        This skill allows the hacker to use the "scan" command. Without having access to the scan command, hackers will be limited in how
                        effective they are at hacking. They can still hack sites, but need to "move" into nodes to find out what it contains.
                        This is dangerous as it can trigger tripwires.
                    </p>
                    <p className="text">
                        Default hacker skill: <strong>yes</strong><br/>This is a core ability that all hackers should have.<br/><br/>
                        The purpose of this skill is to allow the creation of non-professional hacker characters. These characters lack skills, but
                        can still play the game. By having this skill, the professional hackers are actually better.
                    </p>

                    <h5 className="muted">Search site</h5>
                    <p className="text">
                        Default hacker skill: <strong>yes</strong><br/>
                        This skill allows the hacker to initially access a site. Without this skill, there is no search box on the home page of a hacker,
                        and the player cannot start a new hack. Instead, they must be invited to a hack by a hacker that does have this skill.<br/><br/>
                        This is a core ability that all hackers should have.<br/>
                        The purpose of this skill is to allow the creation of non-professional hacker characters. These characters lack skills, but
                        can still play the game. By having this skill, the professional hackers are actually needed to be able to do hacks.
                    </p>

                    <h5 className="muted">Create site</h5>
                    <p className="text">
                        Default hacker skill: <strong>no</strong><br/>
                        This skill allows the hacker to create their own sites. Other hackers can then hack these sites as normal. These can be in-game
                        sites or in-game tutorials to help teach new hackers.<br/><br/>
                        When a hacker has this skill, they will see a menu item "sites" that allows them to manage their sites.
                    </p>

                    {/*<h5 className="muted">Stealth</h5>*/}
                    {/*<p className="text">*/}
                    {/*    Default hacker skill: <strong>no</strong><br/>*/}
                    {/*    This skill increases the time for a site to shutdown when the hacker triggers a tripwire. A stealth value of +30% means that the*/}
                    {/*    total time to shutdown is increased by 30%. For example from 10 minutes to 13 minutes, giving the hackers more time.<br/><br/>*/}
                    {/*    This skill can also be applied negatively, to decrease the time to shutdown. A stealth value of -30% would decrease the time from 10 minutes to 7 minutes.*/}
                    {/*</p>*/}
                    {/*<br/>*/}
                </div>
            </div>
        </div>
    )
}
