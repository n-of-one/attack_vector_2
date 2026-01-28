import React from "react";
import {useSelector} from "react-redux";
import {WordSearchRootState} from "../wordsearch/reducer/WordSearchRootReducer";
import {IceHacker} from "./IceHackersReducer";
import {hackerIconPath} from "../../../common/users/HackerIcon";
import {currentUser} from "../../../common/user/CurrentUser";

interface Props {
    blocked?: string[]
}

/* eslint jsx-a11y/alt-text: 0*/
export const IceHackerPresence = ({blocked = []}: Props) => {

    const hackerState = useSelector((state: WordSearchRootState) => state.hackers)
    const hackerList = Object.values(hackerState).sort((a: IceHacker, b: IceHacker) => a.name.localeCompare(b.name))


    return (
        <div className="text">
            <br/>
            <div>
                {hackerList.map((hacker: IceHacker) => <HackerPresence key={hacker.userId} hacker={hacker}
                                                                       blocked={blocked}/>)}
            </div>
        </div>

    )
}


const HackerPresence = ({hacker, blocked}: { hacker: IceHacker, blocked: string[] }) => {

    const nameClass = blocked.includes(hacker.userId) ? "hacker-blocked" : ""
    const you = currentUser.id === hacker.userId

    return <div>
        <img src={hackerIconPath(hacker.icon, you)} height="30" width="30"/>&nbsp;<span className={nameClass}>{hacker.name}</span>
    </div>


}