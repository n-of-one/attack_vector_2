import {AnyAction} from "redux";

const SERVER_ICE_HACKER_CONNECTED = "SERVER_ICE_HACKER_CONNECTED"
const SERVER_ICE_HACKER_DISCONNECTED = "SERVER_ICE_HACKER_DISCONNECTED"

const SERVER_ICE_HACKERS_UPDATED = "SERVER_ICE_HACKERS_UPDATED"

export interface IceHacker {
    name: string,
    userId: string,
    icon: string,
}

export type IceHackers =  IceHacker[]

export const iceHackerDefaultState: IceHackers = []


export const iceHackersReducer = (state: IceHackers = iceHackerDefaultState, action: AnyAction): IceHackers => {
    switch (action.type) {
        // case SERVER_ICE_HACKER_CONNECTED:
        case SERVER_ICE_HACKERS_UPDATED:
            return action.data
            // return hackerConnected(state, action.data)
        // case SERVER_ICE_HACKER_DISCONNECTED:
            // return hackerDisconnected(state, action.data)
        default:
            return state
    }
}
//
// const hackerConnected = (state: IceHackersState, action: IceHacker): IceHackersState => {
//     const stateCopy = {...state}
//     stateCopy[action.userId] = action
//     return stateCopy
// }
//
// const hackerDisconnected = (state: IceHackersState, action: IceHacker): IceHackersState => {
//     const stateCopy = {...state}
//     delete stateCopy[action.userId]
//     return stateCopy
// }
