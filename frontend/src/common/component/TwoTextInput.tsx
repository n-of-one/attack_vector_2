import React, {ChangeEvent, useState} from 'react';

const ENTER_KEY = "Enter";


interface Props {
    buttonLabel: string,
    buttonClass: string
    save: (value1: string, value2: string) => void,
    placeholder1?: string,
    placeholder2?: string,
    clearAfterSubmit?: boolean
    autofocus?: boolean
    size?: number
    label?: string
    labelColumns?: number
    type1?: string
    type2?: string
}

export const TwoTextInput = (props: Props) => {

    const [text1, setText1] = useState("");
    const [text2, setText2] = useState("");

    const totalButtonClass = "btn " + props.buttonClass;
    const size = props.size || 6;

    const handleChange1 = (event: ChangeEvent<HTMLInputElement>) => {
        setText1(event.target.value);
    }
    const handleChange2 = (event: ChangeEvent<HTMLInputElement>) => {
        setText2(event.target.value);
    }

    const autofocus = !!(props.autofocus) // undefined -> false

    const handleKeyDown = (event: any) => {
        if (event.code === ENTER_KEY) {
            submit();
        }
    }

    const submit = () => {
        props.save(text1, text2);
        if (props.clearAfterSubmit) {
            setText1("");
            setText2("");
        }
    }

    const label = (props.label && props.labelColumns) ? <div className={`col-lg-${props.labelColumns}`}>{props.label}</div> : <></>

    const type1Value = props.type1 || "text"
    const type2Value = props.type2 || "text"

    return (
        <div className="row">
            {label}
            <div className={`col-lg-${size}`} style={{fontSize: "12px"}}>
                <input type={type1Value} className="form-control"
                       placeholder={props.placeholder1}
                       value={text1}
                       onChange={(event) => handleChange1(event)}
                       onKeyDown={(event) => handleKeyDown(event)}
                       autoFocus={autofocus}
                />
            </div>
            <div className={`col-lg-${size}`} style={{fontSize: "12px"}}>
                <input type={type2Value} className="form-control"
                       placeholder={props.placeholder2}
                       value={text2}
                       onChange={(event) => handleChange2(event)}
                       onKeyDown={(event) => handleKeyDown(event)}
                       autoFocus={autofocus}
                />
            </div>
            <div className="col-lg-2">
                <button type="button" className={totalButtonClass} style={{fontSize: "12px"}}
                        onClick={() => submit()}>{props.buttonLabel}</button>
            </div>
        </div>
    );
}


