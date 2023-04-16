import React, {useEffect, useState} from 'react'
import {TangleRoot} from "./tangle/TangleRoot";
import {PasswordRoot} from "./password/PasswordRoot";
import {WordSearchRoot} from "./wordsearch/WordSearchRoot";
import {NetwalkRoot} from "./netwalk/NetwalkRoot";

interface Props {
    redirectId: string
}

export const IceRoot = (props: Props) => {

    const [iceType, setIceType] = useState("")
    const [iceId, setIceId] = useState("")

    useEffect(() => {
        // declare the data fetching function
        const fetchData = async () => {
            const response: Response = await fetch( `/api/ice/${props.redirectId}` );
            const text: string = await response.text()
            const responseObject = JSON.parse(text)
            setIceType(responseObject.type)
            setIceId(responseObject.iceId)
        }

        fetchData()
            .catch(console.error);
    })


    if (iceType === "") {
        return <div style={{color: "yellow"}}>Waiting</div>
    }

    if (iceType === "TANGLE_ICE" ) return <TangleRoot iceId={iceId} />
    if (iceType === "PASSWORD_ICE" ) return <PasswordRoot iceId={iceId} />
    if (iceType === "WORD_SEARCH_ICE" ) return <WordSearchRoot iceId={iceId} />
    if (iceType === "NETWALK_ICE" ) return <NetwalkRoot iceId={iceId} />

    return <div style={{color: "red"}}>No Ice found for ID: {props.redirectId}</div>
}
