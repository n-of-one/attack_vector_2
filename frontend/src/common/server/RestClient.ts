
interface PostCall {
    url: string,
    body?: Object,
    ok: (response: any) => void,
    error: (result: Error | Response) => void,
    method?: string
}

export const restCall = ({url, body, ok, error, method}: PostCall) => {

        fetch(url, {
            method: method ? method : "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(body),
        }
    ).then((response: Response) => {
            if (response.ok) {
                response.text().then(data => {
                    if (data && (data.startsWith("{") || data.startsWith("["))) {
                        const responseData = JSON.parse(data)
                        ok(responseData)
                    }
                    else {
                        console.log("Response was empty or not a JSON object: '" + data + "'")
                        error(response)
                    }
                })
            }
            else {
                error(response)
            }
        }
    ).catch((result: Error) => {
        error(result)
    })
}

export const restGet = ({url, ok, error}: PostCall) => {
    restCall({url, body:undefined, ok, error, method: "GET"})
}


export const restPost = ({url, body, ok, error, }: PostCall) => {
    restCall({url, body, ok, error, method: "POST"})
}


export const restDelete = ({url, body, ok, error}: PostCall) => {
    restCall({url, body, ok, error, method: "DELETE"})
}

