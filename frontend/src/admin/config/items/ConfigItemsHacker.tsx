import React from "react"
import {SilentLink} from "../../../common/component/SilentLink";
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemHackerShowSKills = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: show skills" value={props.value} item={ConfigItem.HACKER_SHOW_SKILLS}/>
            <small className="form-text text-muted">Show the skills of a hacker to the hacker, when they view their user info.<br/><br/>
                Default: false<br/>
                General advice: set to true if your Larp is using skills, and different hackers have different skills.<br/><br/>
                If set to false, only GM can see their skills. This does not affect having or using skills. So if you give a hacker (or all hackers) a skill,
                they can still use it, just not see it.<br/><br/>
                More about skills:<SilentLink href="/website/players-skills"><>here</>
                </SilentLink>.</small><br/>
        </>
    )
}

export const ConfigItemHackerEditUserName = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: edit user name" value={props.value} item={ConfigItem.HACKER_EDIT_USER_NAME}/>
            <small className="form-text text-muted">Allow the hacker to change their user name. Their user name is also how they are shown inside Attack Vector.<br/><br/>
                Default: true<br/>
                General advice: set to true if you want your hackers to choose their user name.<br/><br/>
                If set to false, only the GM can change their user name.</small><br/>
        </>
    )
}

export const ConfigItemHackerEditCharacterName = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: edit character name" value={props.value} item={ConfigItem.HACKER_EDIT_CHARACTER_NAME}/>
            <small className="form-text text-muted">Allow the hacker to change their character name.<br/><br/>
                Default: true<br/><br/>
                Character names are not shown to other hackers. They are only shown to GMs to help them identify the character that is associated with an AV
                user.<br/><br/>
                If set to false, only the GM can change their character name.</small><br/>
        </>
    )
}

export const ConfigItemHackerDeleteRunLinks = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: delete run links" value={props.value} item={ConfigItem.HACKER_DELETE_RUN_LINKS}/>
            <small className="form-text text-muted">Allow the hacker to delete links to runs.<br/><br/>
                Default: true<br/>
                General advice: set to true<br/><br/>
                If set to false, the hackers won't be able to remove links to runs. This is used -for example- on
                attackvector.nl where the system generates a single run link (to their personal tutorial site), and there are no other sites for the hacker to
                hack.</small><br/>
        </>
    )
}

export const ConfigItemHackerTutorialSiteName = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: tutorial site name" value={props.value} item={ConfigItem.HACKER_TUTORIAL_SITE_NAME}/>
            <small className="form-text text-muted">Tutorial site name<br/><br/>
                Default: (empty)<br/>
                General advice: set to "tutorial" if you want to use the default tutorial site.<br/><br/>

                If you want new hackers to automatically start with their personal tutorial site, set the name of this site here.
                This site must be present. (This is how it works on https://attackvector.nl).<br/><br/>
                When installing AttackVector, a tutorial site named "tutorial" will be set up. You can use that name here.<br/><br/>
                If you leave this empty, new hackers will start with an empty page without sites/run-links.<br/>
                </small><br/>
        </>
    )
}

export const ConfigItemHackerScriptRamRefreshDuration = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: Script RAM refresh duration" value={props.value} item={ConfigItem.HACKER_SCRIPT_RAM_REFRESH_DURATION}/>
            <small className="form-text text-muted">Script RAM refresh duration<br/><br/>
                Default: 00:15:00<br/><br/>

                Scripts give hackers a way to make sites easier. To allow GMs to have some control over the amount of scripts a hacker can bring to a single
                site, the RAM of a script is locked for a certain amount of time after it is used.<br/><br/>

                This prevents hackers from quickly loading new scripts after they have used their initial set of scripts. A single block of RAM becomes
                available again after this duration has passed.<br/><br/>

                Setting this value too low will allow hackers to possibly bring multiple scripts to a single run<br/><br/>

                Setting this value too high will prevent hackers from using multiple scripts in consecutive hacks on different sites.<br/><br/>

                See also: Script lockout duration.<br/>
                See also: Script loading during run.
            </small><br/>
        </>
    )
}

export const ConfigItemHackerScriptLockoutDuration = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: Script lockout duration" value={props.value} item={ConfigItem.HACKER_SCRIPT_LOCKOUT_DURATION}/>
            <small className="form-text text-muted">Script lockout duration<br/><br/>
                Default: 01:00:00<br/><br/>

                Scripts give hackers a way to make sites easier. To allow GMs to have some control over the amount of scripts a hacker can bring to a single
                site, hackers cannot load scripts for a certain duration after they have have entered a site.<br/><br/>

                This provides a minimum time window in which a hacker cannot load new scripts.<br/><br/>

                Setting this value too low will allow hackers to possibly bring multiple scripts to a single run<br/><br/>

                Setting this value too high will prevent hackers from using multiple scripts in consecutive hacks on different sites.<br/><br/>

                See also: Script RAM refresh duration.<br/>
                See also: Script loading during run.
            </small><br/>
        </>
    )
}

export const ConfigItemHackerScriptLoadDuringRun = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: Script loading during run" value={props.value} item={ConfigItem.HACKER_SCRIPT_LOAD_DURING_RUN}/>
            <small className="form-text text-muted">Script loading during run<br/><br/>
                Default: false<br/><br/>

                Hackers have a script overlay where they can access their scripts during a run. For each script there are buttons to
                unload/share/delete their scripts. If this value is set to true, it will also include a button to load a script.<br/><br/>

                Allowing hackers to load scripts during a run will make it less predictable for GMs to know how hard a site will be.<br/><br/>

                That said, for most sites, hackers can just disconnect from the site, load scripts using the script page and then connect again.
                So disallowing this is more of a statement than a real prevention.<br/><br/>

                See also: Script RAM refresh duration.<br/>
                See also: Script lockout duration.
            </small><br/>
        </>
    )
}
