import {User} from "./CurrentUserReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {TextSaveInput} from "../component/TextSaveInput";
import React from "react";
import {InfoBadge} from "../component/ToolTip";
import {SilentLink} from "../component/SilentLink";
import {ActionButton} from "../component/ActionButton";
import {HackerSkill, HackerSkillType, skillCanHaveMultipleInstances, skillHasValue, skillInfoText, skillName} from "./HackerSkills";

export const HackerSkillsElement = ({user, readonlySkills}: { user: User, readonlySkills: boolean }) => {
    if (!user.hacker) {
        return <></>
    }

    const sortedSkills = [...user.hacker.skills].sort((a: HackerSkill, b: HackerSkill) => skillName[a.type].localeCompare(skillName[b.type]))

    return <>
        {sortedSkills.map((skill) => {
            return <HackerSkillElement skill={skill} readonly={readonlySkills}/>
        })}
        <HackerAddSkillElement user={user} readonlySkills={readonlySkills} skills={sortedSkills}/>
    </>
}

interface HackerSkillElementProps {
    skill: HackerSkill,
    readonly: boolean,
}

const HackerSkillElement = ({skill, readonly}: HackerSkillElementProps) => {

    const hasValue = skillHasValue[skill.type]
    const infoText = skillInfoText[skill.type]
    const name = skillName[skill.type]

    if (readonly) {
        const valueDisplay = hasValue ? ` : ${skill.value}` : ""
        return <ul>
            <li className="text-muted"><InfoBadge infoText={infoText}/> {name}{valueDisplay}</li>
        </ul>
    }

    const skillValueElement = hasValue ? <SkillValueElement skill={skill}/> : <></>

    return <>
        <div className="row">
            <div className={"col-lg-4"}>
                <SkillDeleteElement skill={skill}/> &nbsp;
                <InfoBadge infoText={infoText}/> {name}
            </div>

            {skillValueElement}
            <br/>
            <br/>
        </div>
    </>
}

const SkillDeleteElement = ({skill}: { skill: HackerSkill }) => {
    const action = () => {
        webSocketConnection.send("/user/skill/delete", {skillId: skill.id})
    }
    return <SilentLink onClick={action} title="Delete"><span className="glyphicon glyphicon-trash"/></SilentLink>
}

const SkillValueElement = ({skill}: { skill: HackerSkill }) => {
    const save = (value: string) => {
        const message = {skillId: skill.id, value}
        webSocketConnection.send("/user/skill/edit", message)
    }

    const skillValueDisplay = skill.value ? skill.value : ""
    return <div className={"col-lg-7"} style={{height: "30px"}}>
            <span style={{position: "relative", top: "-10px"}}>
            <TextSaveInput className="form-control" value={skillValueDisplay} save={save} style={{height: "28px"}}/>
                </span>
    </div>
}


const HackerAddSkillElement = ({user, readonlySkills, skills}: { user: User, readonlySkills: boolean, skills: HackerSkill[] }) => {
    const [chosenSkillOption, setChosenSkillOption] = React.useState<string>("")

    if (readonlySkills) return <></>

    return <>
        <hr/>
        <div className="row form-group text">

            <label htmlFor="addEffect" className="col-lg-3 control-label text-muted">Add skill:</label>
            <div className="col-lg-7">
                <select className="form-control" value={chosenSkillOption}
                        onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                            setChosenSkillOption(event.target.value)
                        }}>
                    <option value=""></option>
                    <SkillOption type={HackerSkillType.SEARCH_SITE} name={"Search Site - allow searching sites"} skills={skills}/>
                    <SkillOption type={HackerSkillType.SCAN} name={"Scan - allow scan command"} skills={skills}/>
                    <SkillOption type={HackerSkillType.CREATE_SITE} name={"Create site - allow creating sites"} skills={skills}/>
                    <SkillOption type={HackerSkillType.SCRIPT_RAM} name={"Script RAM - allow usage of scripts"} skills={skills}/>
                    <SkillOption type={HackerSkillType.STEALTH} name={"Stealth - increase tripwire timers"} skills={skills}/>
                    <SkillOption type={HackerSkillType.BYPASS} name={"Bypass - ignore ICE in first node"} skills={skills}/>
                    <SkillOption type={HackerSkillType.WEAKEN} name={"Weaken - reduce ICE strength"} skills={skills}/>
                    <SkillOption type={HackerSkillType.UNDO_TRIPWIRE} name={"Undo tripwire - undo tripping tripwires"} skills={skills}/>

                </select>
            </div>
            <div className="col-lg-1">
                <ActionButton text="Add" onClick={() => {
                    const message = {userId: user.id, type: chosenSkillOption}
                    webSocketConnection.send("/user/skill/add", message)
                    setChosenSkillOption("")
                }}/>
            </div>
        </div>
    </>
}

const SkillOption = ({type, skills, name}: { type: HackerSkillType, skills: HackerSkill[], name: string }) => {
    const alreadyPresent = skills.find((skill: HackerSkill) => {
        return skill.type === type
    })
    if (alreadyPresent && !skillCanHaveMultipleInstances[type]) return <></>

    return <option value={type}>{name}</option>
}
