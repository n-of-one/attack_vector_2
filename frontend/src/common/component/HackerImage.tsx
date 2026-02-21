import React from 'react';
import {hackerIconPath} from "../users/HackerIcon";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type: string,
    you: boolean,
    onLoad: () => void
    size?: number
}

export const HackerImage = ({type, you, onLoad, size}: Props) => {
    const height = size ? size : 80
    const width = size ? size : 80

    const iconPath = hackerIconPath(type, you)

    const id = you ? `${type}-red` : type

    return (
        <img src={iconPath} height={height} width={width} id={id} onLoad={onLoad}/>
    )
}
