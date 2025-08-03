import React, {useEffect, useState} from "react";
import Cookies from "js-cookie";

/* eslint jsx-a11y/anchor-is-valid: 0*/

interface LolaSpeakResult {
    success: boolean
    message: string
}

export const Lola = (): React.JSX.Element => {

    const [serverMessage, setServerMessage] = useState("")
    const [text, setText] = useState("")
    const [store, setStore] = useState(false)
    const [clear, setClear] = useState(true)
    const [storedText, setStoredText] = useState([] as string[])

    useEffect(() => {
        const cookieValue = Cookies.get("lolaStoredText")
        const storedTextCookie = (cookieValue) ? cookieValue.split("▲") : []
        setStoredText(storedTextCookie)

    }, [setStoredText])

    const submit = async (e: React.SyntheticEvent) => {
        e.preventDefault()
        const toSpeak = text.trim()
        if (text.length > 0) {
            await speak(text)
        }
        if (store) {
            const newStoredText = [...storedText, toSpeak]
            updateStored(newStoredText)
        }
    }

    const speak = async (text: string) => {
        const response = await fetch(`/api/frontier/lola/speak?text=${text}`)
        if (response.ok) {
            const result = await response.json() as LolaSpeakResult
            setServerMessage(result.message)
            if (result.success && clear) {
                setText("")
            }
        } else {
            setServerMessage("Failed to speak, server error. " + response.status)
        }
        setTimeout(() => {
            setServerMessage("")
        }, 1000 * 3)
    }

    const loadStored = (e: React.SyntheticEvent) => {
        e.preventDefault()
        const target = e.target as HTMLAnchorElement
        setText(target.text)
    }

    const deleteStored = (e: React.SyntheticEvent, index: number) => {
        e.preventDefault()
        if (!window.confirm("Delete this stored text: " + storedText[index])) {
            return
        }
        const remainingStored = storedText.filter((_, i) => i !== index)
        updateStored(remainingStored)
    }

    const updateStored = (newStoredText: string[]) => {
        setStoredText(newStoredText)
        setStore(false)
        Cookies.set("lolaStoredText", newStoredText.join("▲"))

    }

    return <div className="container" data-bs-theme="dark">
        <br/>
        <br/>
        <br/>
        <br/>
        <div className="row text">
            <div className="d-flex justify-content-center">
                <h3 className="muted">LOLA console</h3>
            </div>
        </div>

        <br/>
        <br/>

        <div className="row text">
            <div className="d-flex justify-content-center">
                <form style={{width: "90%"}}
                      onSubmit={submit}>
                    <div className="row align-items-center">
                        <div className="col col-lg-12">
                            <textarea id="text" className="form-control" name="text"
                                      rows={3} value={text} onChange={(e) => setText(e.target.value)}
                                      placeholder="" autoFocus={true}/>
                        </div>
                    </div>


                    <br/>

                    <div className="row align-items-center">
                        <div className="col col-lg-6">
                            <button type="submit" className="btn btn-primary button-text"
                            >Speak
                            </button>
                        </div>
                        <div className="col col-lg-3">
                            <input type="checkbox" checked={clear}
                                   onChange={(e) => {
                                       setClear(e.target.checked)
                                   }}
                            /> Clear
                        </div>
                        <div className="col col-lg-3">
                            <input type="checkbox" checked={store}
                                   onChange={(e) => {
                                       setStore(e.target.checked)
                                   }}
                            /> Store
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <br/>
        <div className="row text">
            <div className="d-flex justify-content-center">
                {serverMessage}&nbsp;
            </div>
        </div>
        <br/>
        <br/>
        {
            storedText.map((text, index) => {
                return <>
                    <div className="row text">
                        <div className="col col-lg-12">
                            <a onClick={event => {
                                   deleteStored(event, index)
                               }}><span className="glyphicon glyphicon-trash"/></a>&nbsp;

                            <a onClick={loadStored}
                            >{text}</a>
                        </div>
                    </div>
                    <br/>
                </>
            })
        }
    </div>

}
