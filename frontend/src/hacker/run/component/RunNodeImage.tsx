import React from 'react'
import {useSelector} from "react-redux";
import {HackerState} from "../../HackerRootReducer";
import {NodeFileType} from "../../../common/enums/NodeTypesNames";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type: NodeFileType,
    onLoad: () => void
}

export const RunNodeImage = ({type, onLoad}: Props) => {
    const theme = useSelector((state: HackerState) => state.theme)

    const root = "/img/" + theme + "/nodes/"
    const dirAndName = type.dir + "/" + type.name + ".png"

    const pathDiscovered = root + "empty.png"
    const pathType = root + "type/" + dirAndName
    const pathConnections = root + "connections/" + dirAndName
    const pathFree = root + "free/" + dirAndName
    const pathProtected = root + "protected/" + dirAndName
    const pathHacked = root + "hacked/" + dirAndName

    return (
        <span>
                <img src={pathDiscovered} height="80" width="80" id={type.name + "_DISCOVERED"} onLoad={onLoad}/>
                <img src={pathType} height="80" width="80" id={type.name + "_TYPE"} onLoad={onLoad}/>
                <img src={pathConnections} height="80" width="80" id={type.name + "_CONNECTIONS"} onLoad={onLoad}/>
                <img src={pathFree} height="80" width="80" id={type.name + "_FREE"} onLoad={onLoad}/>
                <img src={pathProtected} height="80" width="80" id={type.name + "_PROTECTED"} onLoad={onLoad}/>
                <img src={pathHacked} height="80" width="80" id={type.name + "_HACKED"} onLoad={onLoad}/>
            </span>
    )
}
