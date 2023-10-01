import React, {useEffect, useState} from "react";
import {TopLevelError} from "../../common/component/TopLevelError";
import {StatusLightRoot} from "./statusLight/StatusLightRoot";

interface ServerResponse {
    appId: string,
    error: string | null,
}

interface Props {
    type: string,
    layerId: string,
}

export const WidgetSelector = ({type, layerId}: Props) => {

    const [response, setResponse] = useState(null as ServerResponse | null)
    useEffect(() => {
        const fetchData = async () => {
            const response: Response = await fetch(`/api/widget/${layerId}`)
            const text: string = await response.text()
            const responseObject = JSON.parse(text)
            setResponse(responseObject)
        }

        fetchData().catch((error: unknown) => {
            setResponse({appId: "", error: String(error)})
        });
    }, []) /* eslint react-hooks/exhaustive-deps: 0 */

    if (response === null) {
        return <div style={{color: "cornsilk"}}>Loading</div>
    }

    if (response.error) {
        return <TopLevelError error="Connection error"
                              description={`(Failed to connect to AV server, try again. ${response.error})`}/>
    }


    switch(type) {
        case "statusLight":
            return <StatusLightRoot appId={response.appId}/>
        default:
            return <TopLevelError error="Invalid connection"
                                  description={`(Unknown widget type: ${type}, maybe the QR code/URL is from an older/different version of Attack Vector?)`}/>
    }

}