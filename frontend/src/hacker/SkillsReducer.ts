import {HackerSkill, HackerSkillType} from "../common/users/UserReducer";
import {AnyAction} from "redux";

const SERVER_RECEIVE_HACKER_SKILLS = "SERVER_RECEIVE_HACKER_SKILLS"

export const skillsReducer = (state: HackerSkill[] | null = null, action: AnyAction): HackerSkill[] | null => {

    switch (action.type) {
        case SERVER_RECEIVE_HACKER_SKILLS:
            return action.data
        default:
            return state
    }
}

export const hasSkill = ( hackerSkills: HackerSkill[], requested: HackerSkillType): boolean => {
    return hackerSkills.some((hackerSKill) => hackerSKill.type === requested)
}
