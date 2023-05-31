import toast, {Toast, Toaster} from 'react-hot-toast';
import React from "react";

const createNotification = (message: string, leftIcon: string, rightIcon: string, duration: number, dismiss = true) => {

    toast((t: Toast) => {
            const dismissMethod = dismiss? () => toast.dismiss(t.id) : () => {}
            return (
                <span className="d-flex justify-content-between" >
                    <span onClick={dismissMethod}>{message}</span>
                    <span className="align-self-center">&nbsp;{rightIcon}</span>
              </span>)
        },
        {
            duration: duration,
            icon: leftIcon,
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
        createNotification(title + ": "+ message, "△", "▽", 5000)
    } else if (type === "neutral") {
        createNotification(title + ": "+ message, "◈", "◈", 5000)
    } else if (type === "fatal") {
        createNotification(message, "◰", "◳", Infinity, false)
    } else { // error and catch-all for server notifications with the wrong type
        createNotification(message, "◬", "◬", Infinity)
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

