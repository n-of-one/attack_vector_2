import React from "react";
import {useSelector} from "react-redux";
import {WordSearchRootState} from "../wordsearch/reducer/WordSearchRootReducer";
import {IceHacker} from "./IceHackersReducer";
import {hackerIconFilename} from "../../../common/canvas/HackerIconFilename";

const imageFileName = (type: string) => {
    const fileName = hackerIconFilename[type]
    const theme = "frontier"
    const root = "/img/" + theme + "/actors/hackers/";
    return root + fileName;
}

/* eslint jsx-a11y/alt-text: 0*/
export const IceHackerPresence = () => {

    const hackerState = useSelector((state: WordSearchRootState) => state.hackers)
    const hackerList = Object.values(hackerState).sort((a: IceHacker, b: IceHacker) => a.name.localeCompare(b.name))

    return (
        <div className="text">
            <br/>
            <div>
                {hackerList.map((hacker: IceHacker) => <div key={hacker.userId}>
                        <img src={imageFileName(hacker.icon)} height="30" width="30"/> {hacker.name}
                    </div>
                )}
            </div>
        </div>

    )
}