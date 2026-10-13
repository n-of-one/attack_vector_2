import React, {useEffect} from "react";
import {restGet} from "../common/server/RestClient";

const allowedNext = /^[a-zA-Z0-9/_\-=]{1,200}$/ // allow only a-z A-Z 0-9 / _ - =   the URL-base64 characters and /

// When running behind a reverse proxy that offloads https, the frontend needs to construct the login URL.
export const redirectToLogin = (next?: string | null) => {
    restGet({
        url: "/openapi/login/path",
        ok: ({path}: { path: string }) => {
            const nextPart = (next && allowedNext.test(next)) ? `?next=${next}` : ""
            window.location.href = `${window.location.origin}${path}${nextPart}`
        },
        error: () => {
            console.error("Failed to retrieve login path from server.")
        }
    })
}

export const RedirectToLogin = () => {
    useEffect(() => {
        redirectToLogin(new URLSearchParams(window.location.search).get("next"))
    }, [])
    return (<></>)
}
