import React from "react";
import {User} from "../../common/users/CurrentUserReducer";
import {Script} from "../../common/script/ScriptModel";

interface Props {
    user: User,
}

export const ScriptsList = ({user}: Props) => {

    const scripts = user.hacker?.scripts || []
    if (scripts.length === 0) {
        return <span className="text-muted">No scripts</span>
    }
    return <>
        {scripts.map((script: Script) => <ScriptElement script={script}/>)}
    </>
}

const ScriptElement = ({script}: { script: Script }) => {
    const usableStyle = script.usable ? {} : {textDecoration: "line-through wavy #aaa 1px"}

    return <>
        <span style={usableStyle}>
            <span className="scriptBadge">{script.code}</span> - <span className="text_light"> {script.name}</span> {script.value}<br/>
        </span>
        <div style={{paddingLeft: "10px"}}>
            <span style={{height: "4px", display: "block"}}><br/></span>
            expiry &nbsp; : <span className="light">{script.timeLeft}</span>
        </div>
        <br/>
    </>
}
