import toast, {Toast, Toaster} from 'react-hot-toast';
import React from "react";

const createNotification = (title: string | undefined, message: string, duration: number, dismiss = true) => {

    const text = title ? title + ": " + message : message


    toast((t: Toast) => {
            const dismissMethod = dismiss ? () => toast.dismiss(t.id) : () => {
            }

            return (<span onClick={dismissMethod} style={{cursor: "pointer"}}>
                <span>
                    <span className="text" style={{color: "white", fontSize: "16px"}}>{text}</span>&nbsp;
                    { dismiss ? <span className="text">[close]</span> : <></>}
                </span>
            </span>)
        },
        {
            duration: duration,
            style: {
                borderRadius: '5px',
                background: '#333',
                color: '#fff',
                border: '1px solid #0008'
            },
        })
}


export type NotificationType = "ok" | "neutral" | "error" | "fatal"

export const notify = ({type, message, title}: { type: NotificationType, title?: string, message: string }) => {

    if (type === "ok") {
        createNotification(title, message, 5000)
    } else if (type === "neutral") {
        createNotification(title, message, 5000)
    } else if (type === "fatal") {
        createNotification(undefined, message, Infinity, false)
    } else { // error and catch-all for server notifications with the wrong type
        createNotification(undefined, message, Infinity)
    }
}


export const ToasterConfig = () => {
    return (
        <Toaster
            toastOptions={{
                duration: Infinity,
            }}
        />
    )
}

