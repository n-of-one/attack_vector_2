import {User} from "./CurrentUserReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {TextSaveInput} from "../component/TextSaveInput";
import React from "react";
import {InfoBadge} from "../component/ToolTip";
import {SilentLink} from "../component/SilentLink";
import {ActionButton} from "../component/ActionButton";
import {HackerSkill, HackerSkillType, skillCanHaveMultipleInstances, skillHasValue, skillInfoText, skillName, joinTraitSkillValue, splitTraitSkillValue} from "./HackerSkills";
import {useSelector} from "react-redux";
import {ConfigItem, ConfigRootState, getConfigAsInt} from "../../admin/config/ConfigReducer";

interface HackerSkillForDisplay {
    id: string,
    type: HackerSkillType,
    value?: string,
    tooltip: string,
}
export const HackerSkillsElement = ({user, readonlySkills}: { user: User, readonlySkills: boolean }) => {
    const config = useSelector((rootState: ConfigRootState) => rootState.config)
    if (!user.hacker) {
        return <></>
    }

    const sortedSkills = [...user.hacker.skills].sort((a: HackerSkill, b: HackerSkill) => skillName[a.type].localeCompare(skillName[b.type]))
    const enrichedSkills = sortedSkills.map((skill: HackerSkill) => {
        if (skill.type !== HackerSkillType.ADJUSTED_SPEED) {
            return {...skill, tooltip: skillInfoText[skill.type]}
        }
        const defaultSpeed = getConfigAsInt(ConfigItem.HACKER_DEFAULT_SPEED, config)
        const tooltip = skillInfoText[skill.type].replace("[DEFAULT_SPEED]", defaultSpeed.toString())
        return {...skill, tooltip}
    })

    return <>
        {enrichedSkills.map((skill) => {
            return <HackerSkillElement skill={skill} readonly={readonlySkills} key={skill.id}/>
        })}
        <HackerAddSkillElement user={user} readonlySkills={readonlySkills} skills={sortedSkills}/>
    </>
}

interface HackerSkillElementProps {
    skill: HackerSkillForDisplay,
    readonly: boolean,
}

const HackerSkillElement = ({skill, readonly}: HackerSkillElementProps) => {

    const hasValue = skillHasValue[skill.type]
    const infoText = skill.tooltip
    const name = skillName[skill.type]

    if (readonly) {
        if (skill.type === HackerSkillType.TRAIT) {
            // The player only sees the description, not the skill name or the key.
            return <ul>
                <li className="text-muted"><InfoBadge infoText='A trait of your character.'/> {splitTraitSkillValue(skill.value).text}</li>
            </ul>
        }
        const valueDisplay = hasValue ? ` : ${skill.value}` : ""
        return <ul>
            <li className="text-muted"><InfoBadge infoText={infoText}/> {name}{valueDisplay}</li>
        </ul>
    }

    const skillValueElement = (skill.type === HackerSkillType.TRAIT) ? <TraitSkillValueElement skill={skill}/> :
        hasValue ? <SkillValueElement skill={skill}/> : <></>

    return <>
        {/* Fixed row height: skills without a value input would otherwise be shorter than those with one (30px), making rows overlap. */}
        <div className="row" data-row="hacker-skill" data-skill={skill.type} style={{minHeight: "30px"}}>
            <div className={"col-lg-5"}>
                <SkillDeleteElement skill={skill}/> &nbsp;
                <InfoBadge infoText={infoText}/> {name}
            </div>

            {skillValueElement}
        </div>
    </>
}

const SkillDeleteElement = ({skill}: { skill: HackerSkill }) => {
    const action = () => {
        webSocketConnection.send("/user/skill/delete", {skillId: skill.id})
    }
    return <SilentLink onClick={action} title="Delete"><span className="glyphicon glyphicon-trash"/></SilentLink>
}

const saveSkillValue = (skill: HackerSkill, value: string) => {
    const message = {skillId: skill.id, value}
    webSocketConnection.send("/user/skill/edit", message)
}

/** The TRAIT skill value is "<key>: <text>". Edit both parts separately, and save them as a single value. */
const TraitSkillValueElement = ({skill}: { skill: HackerSkill }) => {
    const {key, text} = splitTraitSkillValue(skill.value)

    return <>
        <div className={"col-lg-2"} style={{height: "30px"}}>
            <span style={{position: "relative", top: "-10px"}}>
                <TextSaveInput className="form-control" name="traitKey" placeholder="key" value={key} style={{height: "28px"}}
                               save={(newKey: string) => saveSkillValue(skill, joinTraitSkillValue(newKey, text))}/>
            </span>
        </div>
        <div className={"col-lg-4"} style={{height: "30px"}}>
            <span style={{position: "relative", top: "-10px"}}>
                <TextSaveInput className="form-control" name="traitText" placeholder="text shown to player" value={text} style={{height: "28px"}}
                               save={(newText: string) => saveSkillValue(skill, joinTraitSkillValue(key, newText))}/>
            </span>
        </div>
    </>
}

const SkillValueElement = ({skill}: { skill: HackerSkill }) => {
    const save = (value: string) => saveSkillValue(skill, value)

    const skillValueDisplay = skill.value ? skill.value : ""
    return <div className={"col-lg-6"} style={{height: "30px"}}>
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
        <div className="row form-group text" data-row="add-skill">

            <label htmlFor="addSkill" className="col-lg-2 control-label text-muted">Add skill:</label>
            <div className="col-lg-8">
                <select id="addSkill" className="form-control" value={chosenSkillOption}
                        onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                            setChosenSkillOption(event.target.value)
                        }}>
                    <option value=""></option>
                    <SkillOption type={HackerSkillType.ADJUSTED_SPEED} name={"Adjusted speed - adjust hacker speed"} skills={skills}/>
                    <SkillOption type={HackerSkillType.BYPASS} name={"Bypass - ignore ICE in first node"} skills={skills}/>
                    <SkillOption type={HackerSkillType.CREATE_SITE} name={"Create site - allow creating sites"} skills={skills}/>
                    <SkillOption type={HackerSkillType.UNDO_TRIPWIRE} name={"Glitch - undo tripping tripwires"} skills={skills}/>
                    <SkillOption type={HackerSkillType.JUMP_TO_HACKER} name={"Jump - jump to another hacker"} skills={skills}/>
                    <SkillOption type={HackerSkillType.SCAN} name={"Scan - allow scan command"} skills={skills}/>
                    <SkillOption type={HackerSkillType.SEARCH_SITE} name={"Search Site - allow searching sites"} skills={skills}/>
                    <SkillOption type={HackerSkillType.SCRIPT_CREDITS} name={"Script credits - allow selling of data and buying scripts"} skills={skills}/>
                    <SkillOption type={HackerSkillType.SCRIPT_RAM} name={"Script RAM - allow usage of scripts"} skills={skills}/>
                    <SkillOption type={HackerSkillType.STEALTH} name={"Stealth - increase tripwire timers"} skills={skills}/>
                    <SkillOption type={HackerSkillType.TRAIT} name={"Trait - character trait for GMs, players and sites to react to"} skills={skills}/>
                    <SkillOption type={HackerSkillType.WEAKEN} name={"Weaken - reduce ICE strength"} skills={skills}/>

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
