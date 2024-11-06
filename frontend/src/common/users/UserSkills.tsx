import {HackerSkillType, User} from "./UserReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {TextSaveInput} from "../component/TextSaveInput";
import React from "react";

interface Props {
    user: User,
    readonlySkills: boolean,
}

export const UserSkills = ({user, readonlySkills}: Props) => {
    return <>
        <UserSkill user={user} skillType={HackerSkillType.SCAN} skillName="Scan" readonly={readonlySkills} hasValue={false}/>
        <UserSkill user={user} skillType={HackerSkillType.SEARCH_SITE} skillName="Search site" readonly={readonlySkills} hasValue={false}/>
        <UserSkill user={user} skillType={HackerSkillType.CREATE_SITE} skillName="Create site" readonly={readonlySkills} hasValue={false}/>
        {/*<UserSkill user={user} skillType={HackerSkillType.STEALTH} skillName="Stealth" readonly={readonlySkills} hasValue={true}/>*/}
    </>
}


interface UserSkillProps {
    user: User,
    skillType: HackerSkillType,
    skillName: string,
    readonly: boolean,
    hasValue: boolean,
}

const UserSkill = ({user, skillType, skillName, readonly, hasValue}: UserSkillProps) => {
    if (!user.hacker) {
        return <></>
    }// no hacker data

    const skill = user.hacker.skills.find(skill => skill.type === skillType)
    const hasSkill = skill !== undefined

    if (readonly) {
        if (!skill) {
            return <></>
        }
        const valueDisplay = hasValue ? ` : ${skill.value}` : ""
        return <ul>
            <li className="text-muted">{skillName}{valueDisplay}"</li>
        </ul>
    }

    const saveSkillValue = (value: string) => {
        const message = {userId: user.id, type: skillType, value}
        webSocketConnection.send("/user/editSkillValue", message)
    }
    const skillValueDisplay = hasSkill ? (skill.value ? skill.value : "") : ""
    const skillValueElement = hasSkill && hasValue ? <SkillValue currentValue={skillValueDisplay} save={saveSkillValue}/> : <></>


    const saveSkillEnabled = (enable: boolean) => {
        const message = {userId: user.id, type: skillType, enabled: enable}
        webSocketConnection.send("/user/editSkillEnabled", message)
    }

    return <div className="row form-group">
        <div className={`col-lg-1`}/>
        <label htmlFor={skillName} className="col-lg-3 control-label text-muted">{skillName}</label>
        <div className={`col-lg-1`}>
            <input id={skillName} type="checkbox" checked={hasSkill} className="checkbox-inline" onClick={(event: any) => {
                saveSkillEnabled(!hasSkill)
                event.preventDefault()
                return false;
            }} disabled={readonly}/>
        </div>
        {skillValueElement}
        <br/>
        <br/>
    </div>
}

interface SkillValueProps {
    currentValue: string,
    save: (value: string) => void,
}

const SkillValue = ({currentValue, save}: SkillValueProps) => {
    return <div className={`col-lg-2`}>
            <span style={{position: "relative", top: "-10px"}}>
            <TextSaveInput className="form-control" value={currentValue} save={save}/>
                </span>
    </div>
}
