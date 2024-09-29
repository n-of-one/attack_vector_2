import React from 'react'
import {CLOSE_USER_EDIT} from "./UsersReducer";
import {HackerSkill, User, USER_TYPE_ADMIN, USER_TYPE_GM, USER_TYPE_HACKER, USER_TYPE_HACKER_MANAGER} from "./UserReducer";
import {TextSaveInput} from "../component/TextSaveInput";
import {DropDownSaveInput} from "../component/DropDownSaveInput";
import userAuthorizations, {ROLE_GM, ROLE_HACKER_MANAGER, ROLE_USER_MANAGER} from "../user/UserAuthorizations";
import {CloseButton} from "../component/CloseButton";
import {useDispatch} from "react-redux";
import {webSocketConnection} from "../server/WebSocketConnection";
import {currentUser} from "../user/CurrentUser";
import {larp} from "../Larp";
import {HackerIcon} from "./HackerIcon";

type SaveFunction = (field: string, value: string) => void
type SaveSkillFunction = (field: string, value: boolean) => void

interface Props {
    user: User | null,
}

export const UserDetails = ({user}: Props) => {
    if (!user) {
        return <></>
    }
    const readOnlyType: boolean = !userAuthorizations.hasRole(ROLE_USER_MANAGER)

    return <UserDetailsInternal user={user} readOnlyType={readOnlyType}/>
}

interface PropsInternal {
    user: User,
    readOnlyType: boolean
}

const UserDetailsInternal = ({user, readOnlyType}: PropsInternal) => {
    const dispatch = useDispatch()
    const closeUserEdit = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    const save = (field: string, value: string) => {
        const message = {userId: user.id, field, value}
        webSocketConnection.send("/user/edit", message)
    }

    const readonlyUserName = !larp.hackerEditUserName

    const isGm = userAuthorizations.hasRole(ROLE_GM)

    const closeButton = isGm ? <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={closeUserEdit}/></div> : <></>

    return <>
        {closeButton}

        <hr/>

        <UserAttribute
            label="Name" id="name" size={4}
            value={user.name}
            save={save} attributeSaveName="name"
            readonly={readonlyUserName}
        />

        <div className="row form-group">
            <label htmlFor="type" className="col-lg-4 control-label text-muted">Type</label>
            <div className="col-lg-4">
                <DropDownSaveInput id="type" className="form-control"
                                   selectedValue={user.type}
                                   save={(value: string) => save("type", value)}
                                   readonly={readOnlyType}>
                    <>
                        <option value={USER_TYPE_HACKER}>Hacker</option>
                        <option value={USER_TYPE_HACKER_MANAGER}>Hacker manager</option>
                        <option value={USER_TYPE_GM}>Game master</option>
                        <option value={USER_TYPE_ADMIN}>Admin</option>
                    </>
                </DropDownSaveInput>
            </div>
        </div>
        {(user.type === USER_TYPE_HACKER || user.type === USER_TYPE_HACKER_MANAGER) ? hackerFormPart(user, save) : <></>}
        {deleteUserButton(user, closeUserEdit)}
    </>
}

const hackerFormPart = (user: User, save: SaveFunction) => {

    const authorizationToEditSkills = userAuthorizations.hasRole(ROLE_USER_MANAGER) || userAuthorizations.hasRole(ROLE_HACKER_MANAGER)
    const readonlySkills = !(larp.hackerEditSkills && authorizationToEditSkills)
    const showSkills = (larp.hackerShowSkills || authorizationToEditSkills)

    const saveSkill = (skill: string, value: boolean) => {
        const message = {userId: user.id, skill, value}
        webSocketConnection.send("/user/editSkill", message)
    }

    return <>
        <hr/>
        <UserAttribute
            label="Character Name" id="characterName" size={4}
            value={user.hacker?.characterName}
            save={save} attributeSaveName="characterName"
            readonly={!larp.hackerEditCharacterName}
        />

        <div className="row form-group">
            <label htmlFor="type" className="col-lg-4 control-label text-muted">Icon</label>
            <div className="col-lg-4">
                <DropDownSaveInput id="type" className="form-control"
                                   selectedValue={user.hacker!!.icon}
                                   save={(value: string) => save("hackerIcon", value)}
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

            <UserSkill user={user} skill={HackerSkill.SEARCH_SITE} skillName="Search Site" save={saveSkill} field="SEARCH_SITE" readonly={readonlySkills}/>
            <UserSkill user={user} skill={HackerSkill.SCAN} skillName="Scan" save={saveSkill} field="SCAN" readonly={readonlySkills}/>
        </> : <></>}


    </>
}

interface UserAttributeProps {
    label: string,
    id: string,
    size: number,
    value?: string | number,
    save: SaveFunction,
    attributeSaveName: string
    readonly: boolean,
    indent?: number,
    show?: boolean
}

const UserAttribute = ({label, id, size, value, readonly, save, attributeSaveName, indent, show}: UserAttributeProps) => {
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
                           save={(value: string) => save(attributeSaveName, value)}
                           readonly={readonly}/>
        </div>
    </div>
}

const deleteUserButton = (user: User, closeUserEdit: () => void) => {
    if (user.id === currentUser.id) return null

    const deleteFun = () => {
        const confirmResult = window.confirm("Are you sure you want to delete user " + user.name + "?")
        if (!confirmResult) return
        webSocketConnection.send("/user/delete", user.id)
        closeUserEdit()
    }

    return <>
        <hr/>
        <div className="d-flex flex-row">
            <button className="btn btn-info btn-sm pull-right" onClick={deleteFun}>Delete</button>
        </div>
    </>
}

const hackerIconOptions = () => {
    const options = Object.keys(HackerIcon)
        .map((icon) => {
            return <option key={icon} value={icon}>{icon}</option>
        })
    return options
}


interface UserSkillProps {
    user: User,
    skill: HackerSkill,
    skillName: string,
    field: string
    save: SaveSkillFunction,
    readonly: boolean,
}

const UserSkill = ({user, skill, skillName, field, readonly, save}: UserSkillProps) => {

    const checked = user.hacker?.skills?.includes(skill)

    if (readonly) {
        if (!checked) { return <></> }
        return <ul><li className="text-muted">{skillName}</li></ul>
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
