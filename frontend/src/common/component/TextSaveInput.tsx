import React, {useState} from 'react';

const ENTER_KEY = 13;

/**
 * When the focus is lost or enter is hit, the component fires its "save" method as provided by the props.
 *
 * During the saving, a floppy disk icon is shown.
 */

/*
This Component is almost a "fully uncontrolled component with a key"
as described in: https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html

However, it still needs to know when the save has finished, so that it can remove the floppy disk icon.
For this, the componentWillReceiveProps method is used.

The key needs to be set at a higher level, where we have context to base this key on.
 */

interface Props {
    id?: string,
    value?: string | number,
    placeholder?: string,
    type?: string, // textArea or undefined for textInput
    save: (value: string) => void,
    className: string,
    rows?: number,
    readonly?: boolean,

}


export const TextSaveInput = (props: Props) => {
    const readonly = props.readonly ? props.readonly : false
    // convert potential number values to string, otherwise equals detection does not work
    const valueString = "" + props.value


    const [value, setValue] = useState(valueString);

    const [editing, setEditing] = useState(false);
    const [saving, setSaving] = useState(false);

    if (!editing && valueString !== value) {
        setValue(valueString);
    }

    if (valueString === value && saving) {
        setSaving(false);
    }
    const handleChange = (event: any) => {
        const newValue = event.target.value;
        setValue(newValue)
        setSaving(false)
    };

    const handleKeyDown = (event: any, defocusOnEnter: boolean) => {
        if (!editing) { setEditing(true); }
        if (event.keyCode === ENTER_KEY && defocusOnEnter) {
            event.target.blur();
        }
    };

    const handleBlur = () => {
        console.log("Blur, value:" + value + " , props.value:" + props.value);
        if (value !== props.value) {
            props.save(value);
            setSaving(true);
        }
    };


    // Don't have text be null or undefined, this will cause React to treat the Input as
    // an uncontrolled component and then later detect that it is now a controlled component.
    // This will log errors in the console
    let text = value;
    if (text === null || text === undefined) {
        text = '';
    }

    let icon = (saving) ? (<span className="form-control-feedback d-flex justify-content-end"><span className="glyphicon glyphicon-floppy-save"
                                                                         aria-hidden="true"/></span>) : '';

    if (props.type === "textArea") {
        return (
            <span>
                <textarea id={props.id}
                          className={props.className}
                          placeholder={props.placeholder}
                          value={text}
                          onChange={(event) => handleChange(event)}
                          onKeyDown={(event) => handleKeyDown(event, false)}
                          onBlur={() => handleBlur()}
                          rows={props.rows}
                          disabled={readonly}
                />
                {icon}
            </span>
        );
    } else {
        return (
            <span>
                    <input id={props.id}
                           type="text" className={props.className}
                           placeholder={props.placeholder}
                           value={text}
                           onChange={(event) => handleChange(event)}
                           onKeyDown={(event) => handleKeyDown(event, true)}
                           onBlur={() => handleBlur()}
                           disabled={readonly}
                    />
                {icon}
                </span>
        );
    }
}
