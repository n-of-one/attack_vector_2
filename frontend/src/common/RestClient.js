
let post = ({url, body, ok, notok, error}) => {

    fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        }
    ).then(response => {
            if (response.ok) {
                response.text().then(data => {
                    const responseData = JSON.parse(data);
                    ok(responseData);
                });
            }
            else {
                if (notok) {
                    notok(response);
                }
                else {
                    console.log("Post request failed to '" + url + "' but no not-ok handling supplied.");
                }
            }
        }
    ).catch(result => {
        if (error) {
            error(result)
        }
        else if (notok) {
            notok(result);
        }
        else {
            console.log("Post request failed to '" + url + "' but no error or not-ok handling supplied.");
            console.log(result);
        }
    });
};

export {
    post
}