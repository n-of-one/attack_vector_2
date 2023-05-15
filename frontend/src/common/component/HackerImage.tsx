import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../hacker/HackerRootReducer";
import {hackerIconFilename} from "../canvas/HackerIconFilename";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type: string,
    onLoad: () => void
}

export const HackerImage = ({type, onLoad}: Props) => {

    const fileName = hackerIconFilename[type]

    const theme = useSelector((state: HackerState) => state.theme)

    const root = "/img/" + theme + "/actors/hackers/";
    const dirAndName = root + fileName;

    return (
        <img src={dirAndName} height="80" width="80" id={type} onLoad={onLoad}/>
    )
}
