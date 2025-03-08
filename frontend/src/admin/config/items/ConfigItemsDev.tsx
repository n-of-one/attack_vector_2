import React from "react"
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemHackerUseDevCommands = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Development: hackers can use dev commands" value={props.value} item={ConfigItem.DEV_HACKER_USE_DEV_COMMANDS}/>
            <small className="form-text text-muted">Can hackers use commands that are only intended for developers?<br/><br/>
                Default: false<br/>
                General advice: leave this to false.<br/><br/>
                There are a number of terminal commands that are very overpowered, and not intended to be part of the game.
                They are used to make it quicker to test during development.
                These commands include: quickscan (qs), quickattack (qa), quickhack, qmove, sweeperunblock.
            </small>
        </>
    )
}

export const ConfigItemHackerResetSite = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Development: hackers can reset sites" value={props.value} item={ConfigItem.DEV_HACKER_RESET_SITE}/>
            <small className="form-text text-muted">Can hackers reset a site? Resetting a site is normally caused by tripping a tripwire, or by a GM.<br/><br/>
                Default: false<br/>
                General advice: leave this to false.<br/><br/>
                Having a hacker be able to reset a site is mostly useful during development, where you want to quickly run test against a single site multiple
                times.
                Often you want the site to be in it's reset state, and you don't want to log in as a GM to do this.<br/><br/>This is also used for
                attackvector.nl where there is
                only the tutorial, and this way the players can replay the tutorial.
            </small>
        </>
    )
}

export const ConfigItemQuickPlaying = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Development: quick playing" value={props.value} item={ConfigItem.DEV_QUICK_PLAYING}/>
            <small className="form-text text-muted">Skip most of the flavour text when accessing ICE.<br/><br/>
                Default: false<br/>
                General advice: leave this to false.<br/><br/>
                Setting this to true will skip most of the flavour text when opening an ICE puzzle. This is convenient during development, but not
                intended for normal play.
            </small>
        </>
    )
}

export const ConfigItemDevSimulateNonLocalHost = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Development: delay ms for simulating non-localhost" value={props.value}
                            item={ConfigItem.DEV_SIMULATE_NON_LOCALHOST_DELAY_MS}/>
            <small className="form-text text-muted">Artificial delay when responding to network calls<br/><br/>
                Default: 0<br/>
                General advice: leave this to 0.<br/><br/>
                If you are running attack vector on a local machine, and want to know how responsive it will be when running on the cloud, you can set this
                value to the number of milliseconds of artificial delay you want. Suggested value: 70
            </small>
        </>
    )
}
