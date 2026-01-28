import React, {useState} from 'react';


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
    selectedValue?: string,
    save: (value: string) => void,
    className: string,
    children?: JSX.Element,
    readonly?: boolean
}


export const DropDownSaveInput = (props: Props) => {

    const readonly = props.readonly ? props.readonly : false

    // convert potential number values to string, otherwise equals detection does not work
    const propsValue = "" + props.selectedValue

    const [userValue, setUserValue] = useState(propsValue)

    const [saving, setSaving] = useState(false)

    if (propsValue === userValue && saving) {
        setSaving(false)
    }

    const handleChange = (event: any) => {
        const newValue = event.target.value;
        setUserValue(newValue)
        setSaving(true)
        props.save(newValue)
    }

    let icon = (saving) ? (<span className="form-control-feedback d-flex justify-content-end"><span className="glyphicon glyphicon-floppy-save saveIcon"
                                                                                                    aria-hidden="true"/></span>) : ''


    return (
        <span>
            <select id={props.id}
                    value={props.selectedValue}
                    className={props.className}
                    onChange={(event) => handleChange(event)}
                    disabled={readonly}
            >
                {props.children}
            </select>
            {icon}
        </span>
    )
}
