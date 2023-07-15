import React, {useEffect, useState} from "react"
import {SwitchRoot} from "./switch/SwitchRoot";
import {TopLevelError} from "../common/component/TopLevelError";
import {IceRoot} from "../ice/IceRoot";
import {AuthAppRoot} from "./authApp/AuthAppRoot";
import {Query} from "../common/util/Query";

interface Props {
    appId: string,
    query: string
}

type Action = "app" | "auth" | "CONNECT_ERROR"

interface ServerResponse {
    iceId: string,
    type: string,
    action: Action,
    nextUrl: string,
}

export const AppRoot = (props: Props) => {

    const [response, setResponse] = useState(null as ServerResponse | null)

    const query = new Query(props.query)


    useEffect(() => {
        const fetchData = async () => {
            const response: Response = await fetch(`/api/app/${props.appId}?${props.query}`)
            const text: string = await response.text()
            const responseObject = JSON.parse(text)
            setResponse(responseObject)
        }

        fetchData().catch(() => {
            setResponse({iceId: "", type: "", action: "CONNECT_ERROR", nextUrl: ""})
        });
    }, [props.appId, props.query])

    if (response === null) {
        return <div style={{color: "cornsilk"}}>Loading</div>
    }

    if (response.action === "CONNECT_ERROR") {
        return <TopLevelError error="Connection error"
                              description={`(Failed to connect to AV server, try again)`}/>
    }

    if (response.action === "app") {
        return <SwitchRoot appId={props.appId}/>
    }

    if (response.action === "auth") {

        const hacking = query.value("hacking")

        if (hacking === "true") {
            return <IceRoot iceId={response.iceId} nextUrl={response.nextUrl}/>
        } else {
            return <AuthAppRoot iceId={response.iceId} appId={props.appId} nextUrl={response.nextUrl}/>
        }
    }

    return <TopLevelError error="Server error"
                          description={`(Uknown response app type: ${response.action})`}/>
}
