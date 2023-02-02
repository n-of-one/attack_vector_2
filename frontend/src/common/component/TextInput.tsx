import React, {ChangeEvent, useState} from 'react';

const ENTER_KEY = "Enter";


interface Props {
    buttonLabel: string,
    buttonClass: string
    save: (value: string) => void,
    placeholder?: string,
    clearAfterSubmit?: boolean
    autofocus?: boolean
}

export const TextInput = (props: Props) => {

    const [text, setText] = useState("");

    const totalButtonClass = "btn " + props.buttonClass;

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

    return (
        <div className="row">
            <div className="col-lg-6" style={{fontSize: "12px"}}>
                <input type="text" className="form-control"
                       placeholder={props.placeholder}
                       value={text}
                       onChange={(event) => handleChange(event)}
                       onKeyDown={(event) => handleKeyDown(event)}
                       autoFocus={autofocus}
                />
            </div>
            <div className="col-lg-6">
                <button type="button" className={totalButtonClass} style={{fontSize: "12px"}}
                        onClick={() => submit()}>{props.buttonLabel}</button>
            </div>
        </div>
    );
}


