import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../hacker/HackerRootReducer";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    fileName: string,
    type: string,
    onLoad: () => void
}

export const PatrollerImage = ({fileName, type, onLoad}: Props) => {

    const theme = useSelector((state: HackerState)=> state.theme)

    const root = "/img/" + theme + "/actors/patrollers/";
    const dirAndName = root + fileName;


    return (
        <img src={dirAndName} id={type} onLoad={onLoad}/>
    )
}
