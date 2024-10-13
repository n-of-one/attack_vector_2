import React from "react"
import {SilentLink} from "../../../common/component/SilentLink";
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemHackerShowSKills = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Hacker: show skills" value={props.value} item={ConfigItem.HACKER_SHOW_SKILLS}/>
            <small className="form-text text-muted">Show the skills of a hacker to the hacker, when they view their user info.<br/><br/>
                Default: false<br/><br/>
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
                Default: true<br/><br/>
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
                Default: true<br/><br/>
                General advice: set to true<br/><br/>
                If set to false, the hackers won't be able to remove links to runs. This is used -for example- on
                attackvector.nl where the system generates a single run link (to their personal tutorial site), and there are no other sites for the hacker to
                hack.</small><br/>
        </>
    )
}

