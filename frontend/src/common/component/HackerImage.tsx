import React from 'react';
import {hackerIconPath} from "../users/HackerIcon";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type: string,
    you: boolean,
    onLoad: () => void
}

export const HackerImage = ({type, you, onLoad}: Props) => {

    const iconPath = hackerIconPath(type, you)

    const id = you ? `${type}-red` : type

    return (
        <img src={iconPath} height="80" width="80" id={id} onLoad={onLoad}/>
    )
}
