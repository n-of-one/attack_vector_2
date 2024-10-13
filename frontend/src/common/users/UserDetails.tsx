import React from 'react'
import {CLOSE_USER_EDIT} from "./UsersReducer";
import {HackerSkill, User, UserType} from "./UserReducer";
import {TextSaveInput} from "../component/TextSaveInput";
import {DropDownSaveInput} from "../component/DropDownSaveInput";
import userAuthorizations, {ROLE_GM, ROLE_HACKER_MANAGER, ROLE_USER_MANAGER} from "../user/UserAuthorizations";
import {CloseButton} from "../component/CloseButton";
import {useDispatch, useSelector} from "react-redux";
import {webSocketConnection} from "../server/WebSocketConnection";
import {currentUser} from "../user/CurrentUser";
import {HackerIcon} from "./HackerIcon";
import {ConfigItem, ConfigRootState, getConfigAsBoolean} from "../../admin/config/ConfigReducer";

const save = (userId: string, field: string, value: string) => {
    const message = {userId: userId, field, value}
    webSocketConnection.send("/user/edit", message)
}

interface Props {
    user: User,
}

export const UserDetails = ({user}: Props) => {
    const allowChangeRole: boolean = userAuthorizations.hasRole(ROLE_USER_MANAGER) && user.type !== UserType.SKILL_TEMPLATE

    const config = useSelector((rootState: ConfigRootState) => rootState.config)
    const readonlyUserName = !(getConfigAsBoolean(ConfigItem.HACKER_EDIT_USER_NAME, config))

    const dispatch = useDispatch()
    const closeUserEdit = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    const isGm = userAuthorizations.hasRole(ROLE_GM)
    const showHackerDetails = (user.type === UserType.HACKER || user.type === UserType.SKILL_TEMPLATE)
    const closeButton = isGm ? <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={closeUserEdit}/></div> : <></>

    return <>
        {closeButton}

        <hr/>

        <UserAttribute
            user={user}
            label="Name" id="name" size={4}
            value={user.name}
            attributeSaveName="name"
            readonly={readonlyUserName}
        />

        <div className="row form-group">
            <label htmlFor="type" className="col-lg-4 control-label text-muted">Type</label>
            <div className="col-lg-4">
                <DropDownSaveInput id="type" className="form-control"
                                   selectedValue={user.type}
                                   save={(value: string) => save(user.id, "type", value)}
                                   readonly={!allowChangeRole}>
                    <>
                        <option value={UserType.HACKER}>Hacker</option>
                        <option value={UserType.GM}>Game master</option>
                        <option value={UserType.ADMIN}>Admin</option>
                        <option value={UserType.SKILL_TEMPLATE}>Skill template</option>
                    </>
                </DropDownSaveInput>
            </div>
        </div>
        {showHackerDetails ? <HackerDetails user={user}/> : <></>}
        {deleteUserButton(user, closeUserEdit)}
    </>
}

const HackerDetails = ({user}: { user: User }) => {
    const config = useSelector((rootState: ConfigRootState) => rootState.config)
    const hackerShowSkills = getConfigAsBoolean(ConfigItem.HACKER_SHOW_SKILLS, config)
    const hackerEditCharacterName = getConfigAsBoolean(ConfigItem.HACKER_EDIT_CHARACTER_NAME, config)

    const authorizationToEditSkills = userAuthorizations.hasRole(ROLE_USER_MANAGER) || userAuthorizations.hasRole(ROLE_HACKER_MANAGER)
    const readonlySkills = !(authorizationToEditSkills)
    const showSkills = (hackerShowSkills || authorizationToEditSkills)


    return <>
        <hr/>
        <UserAttribute
            user={user}
            label="Character Name" id="characterName" size={4}
            value={user.hacker?.characterName}
            attributeSaveName="characterName"
            readonly={!hackerEditCharacterName}
        />

        <div className="row form-group">
            <label htmlFor="type" className="col-lg-4 control-label text-muted">Icon</label>
            <div className="col-lg-4">
                <DropDownSaveInput id="type" className="form-control"
                                   selectedValue={user.hacker!!.icon}
                                   save={(value: string) => save(user.id, "hackerIcon", value)}
                                   readonly={false}>
                    <>{hackerIconOptions()}
                    </>
                </DropDownSaveInput>
            </div>
        </div>

        {showSkills ? <>
            <hr/>
            <div className="text">Skills</div>
            <br/>

            <UserSkill user={user} skill={HackerSkill.SCAN} skillName="Scan" field="SCAN" readonly={readonlySkills}/>
            <UserSkill user={user} skill={HackerSkill.SEARCH_SITE} skillName="Search Site" field="SEARCH_SITE" readonly={readonlySkills}/>
            <UserSkill user={user} skill={HackerSkill.CREATE_SITE} skillName="Create Site" field="CREATE_SITE" readonly={readonlySkills}/>
        </> : <></>}
    </>
}

interface UserAttributeProps {
    user: User,
    label: string,
    id: string,
    size: number,
    value?: string | number,
    attributeSaveName: string
    readonly: boolean,
    indent?: number,
    show?: boolean
}

const UserAttribute = ({user, label, id, size, value, readonly, attributeSaveName, indent, show}: UserAttributeProps) => {
    if (show === false) return <></>

    const labelIndentElement = indent ? <div className={`col-lg-${indent}`}/> : <></>
    const labelSize = indent ? 4 - indent : 4
    const labelClass = `col-lg-${labelSize} control-label text-muted`

    return <div className="row form-group">
        {labelIndentElement}
        <label htmlFor={id} className={labelClass}>{label}</label>
        <div className={`col-lg-${size}`}>
            <TextSaveInput id={id} className="form-control"
                           placeholder="" value={value}
                           save={(value: string) => save(user.id, attributeSaveName, value)}
                           readonly={readonly}/>
        </div>
    </div>
}

const deleteUserButton = (user: User, closeUserEdit: () => void) => {
    if (user.id === currentUser.id) return null

    const deleteFunction = () => {
        const confirmResult = window.confirm("Are you sure you want to delete user " + user.name + "?")
        if (!confirmResult) return
        webSocketConnection.send("/user/delete", user.id)
        closeUserEdit()
    }

    return <>
        <hr/>
        <div className="d-flex flex-row">
            <button className="btn btn-info btn-sm pull-right" onClick={deleteFunction}>Delete</button>
        </div>
    </>
}

const hackerIconOptions = () => {
    return Object.keys(HackerIcon)
        .map((icon) => {
            return <option key={icon} value={icon}>{icon}</option>
        })
}

interface UserSkillProps {
    user: User,
    skill: HackerSkill,
    skillName: string,
    field: string
    readonly: boolean,
}

const UserSkill = ({user, skill, skillName, field, readonly}: UserSkillProps) => {

    const checked = user.hacker?.skills?.includes(skill)

    if (readonly) {
        if (!checked) {
            return <></>
        }
        return <ul>
            <li className="text-muted">{skillName}</li>
        </ul>
    }

    const save = (skill: string, value: boolean) => {
        const message = {userId: user.id, skill, value}
        webSocketConnection.send("/user/editSkill", message)
    }

    return <div className="row form-group">
        <div className={`col-lg-1`}/>
        <label htmlFor={skillName} className="col-lg-3 control-label text-muted">{skillName}</label>
        <div className={`col-lg-2`}>
            <input id={skillName} type="checkbox" checked={checked} className="checkbox-inline" onClick={(event: any) => {
                save(field, !checked)
                event.preventDefault()
                return false;
            }} disabled={readonly}/>
        </div>
        <br/>
        <br/>
    </div>
}
