import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../hacker/HackerRootReducer";
import {hackerIconPath} from "../users/HackerIcon";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type: string,
    onLoad: () => void
}

export const HackerImage = ({type, onLoad}: Props) => {

    const iconPath = hackerIconPath(type)

    return (
        <img src={iconPath} height="80" width="80" id={type} onLoad={onLoad}/>
    )
}
