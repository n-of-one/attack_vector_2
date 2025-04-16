import toast, {Toast, Toaster, ToastPosition} from 'react-hot-toast';
import React from "react";

export const createNotification = (title: string | undefined, message: string, duration: number, location: ToastPosition="top-right", dismiss = true) => {

    const text = title ? title + ": " + message : message


    toast((t: Toast) => {
            const dismissMethod = dismiss ? () => toast.dismiss(t.id) : () => {
            }

            return (<span onClick={dismissMethod} style={{cursor: "pointer"}}>
                <span>
                    <div className="text" style={{color: "white", fontSize: "16px"}}>{text}</div>&nbsp;
                    { dismiss ? <div className="text">[close]</div> : <></>}
                </span>
            </span>)
        },
        {
            duration: duration,
            position: location,
            style: {
                display: "table",
                borderRadius: '5px',
                background: '#333',
                color: '#fff',
                border: '1px solid #0008',
                // top: '200px',
                // position: 'relative',
            },
        })
}


export type NotificationType = "ok" | "neutral" | "error" | "fatal"

export const notify = ({type, message, title}: { type: NotificationType, title?: string, message: string }) => {


    if (type === "ok") {
        createNotification(title, message, 8000)
    } else if (type === "neutral") {
        createNotification(title, message, 8000)
    } else if (type === "fatal") {
        createNotification("Fatal", message, Infinity, "top-center", false)
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

