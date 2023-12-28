import React, {useEffect, useState} from "react"
import {SwitchRoot} from "./switch/SwitchRoot";
import {TopLevelError} from "../../common/component/TopLevelError";
import {AuthRoot} from "./auth/AuthRoot";
import {avEncodedUrl} from "../../common/util/Util";


interface ServerResponse {
    appId: string,
    redirectLayerId: string | null,
    error: string | null,
}


interface Props {
    type: string,
    layerId: string,
}

export const AppSelector = ({type, layerId}: Props) => {

    const [response, setResponse] = useState(null as ServerResponse | null)

    useEffect(() => {
        const fetchData = async () => {
            const response: Response = await fetch(`/api/app/${layerId}`)
            const text: string = await response.text()
            const responseObject = JSON.parse(text)
            setResponse(responseObject)
        }

        fetchData().catch((error: unknown) => {
            setResponse({appId: "", redirectLayerId: "", error: String(error)})
        });
    }, []) /* eslint react-hooks/exhaustive-deps: 0 */

    if (response === null) {
        return <div style={{color: "dimgrey"}}>Loading</div>
    }

    if (response.error) {
        return <TopLevelError error="Error retrieving data from server"
                              description={`(server: ${response.error})`}/>
    }

    if (response.redirectLayerId) {
        const path = `app/auth/${response.redirectLayerId}`
        const url = avEncodedUrl(path)
        window.location.href = url
        return <div />
    }

    switch (type) {
        case "switch":
            return <SwitchRoot layerId={layerId}/>
        case "auth":
            return <AuthRoot iceId={response.appId} layerId={layerId}/>
        default:
            return <TopLevelError error="Server error"
                                  description={`(Unknown app type: ${type})`}/>
    }
}
