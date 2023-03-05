import React, {useEffect, useState} from 'react'
import {TangleRoot} from "./tangle/TangleRoot";

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
            setIceType(text)
            setIceId(`${text}-${props.redirectId}`)
        }

        fetchData()
            .catch(console.error);
    })


    if (iceType === "") {
        return <div style={{color: "yellow"}}>Waiting</div>
    }

    if (iceType === "tangle" ) return <TangleRoot iceId={iceId} />

    return <div style={{color: "red"}}>Unsupported Ice type: {iceType}</div>
}
