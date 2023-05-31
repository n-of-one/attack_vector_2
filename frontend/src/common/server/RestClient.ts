
interface PostCall {
    url: string,
    body: Object,
    ok: (response: any) => void,
    notok: (response: Response) => void,
    error: (result: Error) => void,
    method?: string
}

export const restCall = ({url, body, ok, notok, error, method}: PostCall) => {

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
                        notok(response)
                    }
                })
            }
            else {
                if (notok) {
                    notok(response)
                }
                else {
                    console.log("Post request failed to '" + url + "' but no not-ok handling supplied.")
                }
            }
        }
    ).catch((result: Error) => {
        if (error) {
            error(result)
        }
        // else if (notok) {
        //     notok(result)
        // }
        else {
            console.log("Post request failed to '" + url + "' but no error or not-ok handling supplied.")
            console.log(result)
        }
    })
}


export const post = ({url, body, ok, notok, error, }: PostCall) => {
    restCall({url, body, ok, notok, error, method: "POST"})
}


export const deleteCall = ({url, body, ok, notok, error}: PostCall) => {
    restCall({url, body, ok, notok, error, method: "DELETE"})
}

