import React, {ChangeEvent, useState} from 'react';

const ENTER_KEY = "Enter";


interface Props {
    buttonLabel: string,
    buttonClass: string
    save: (value: string) => void,
    placeholder?: string,
    clearAfterSubmit?: boolean
    autofocus?: boolean
    size?: number
    label?: string
    labelColumns?: number
    type?: string
    name?: string
}

export const TextInput = (props: Props) => {

    const [text, setText] = useState("");

    const totalButtonClass = "btn " + props.buttonClass;
    const size = props.size || 6;

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        setText(event.target.value);
    }

    const autofocus = !!(props.autofocus) // undefined -> false

    const handleKeyDown = (event: any) => {
        if (event.code === ENTER_KEY) {
            submit();
        }
    }

    const submit = () => {
        props.save(text);
        if (props.clearAfterSubmit) {
            setText("");
        }
    }

    const label = (props.label && props.labelColumns) ? <div className={`col-lg-${props.labelColumns}`}>{props.label}</div> : <></>
    const inputType = props.type || "text"

    return (
        <div className="row">
            {label}
            <div className={`col-lg-${size}`} style={{fontSize: "12px"}}>
                <input type={inputType} className="form-control"
                       placeholder={props.placeholder}
                       value={text}
                       onChange={(event) => handleChange(event)}
                       onKeyDown={(event) => handleKeyDown(event)}
                       autoFocus={autofocus}
                       name={props.name}
                />
            </div>
            <div className="col-lg-2">
                <button type="button" className={totalButtonClass} style={{fontSize: "12px"}}
                        onClick={() => submit()}>{props.buttonLabel}</button>
            </div>
        </div>
    );
}


