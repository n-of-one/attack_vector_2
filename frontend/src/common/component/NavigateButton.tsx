import React from "react";
import {NAVIGATE_PAGE, Page} from "../menu/pageReducer";
import {useDispatch, useSelector} from "react-redux";
import {GenericUserRootState} from "../users/CurrentUserReducer";
import {ActionButton} from "./ActionButton";


interface NavigateButtonProps {
    label: string,
    page: Page,
}

export const NavigateButton = (props: NavigateButtonProps) => {
    const dispatch = useDispatch()
    const currentPage = useSelector((state: GenericUserRootState) => state.currentPage)

    const action = () => {
        dispatch({type: NAVIGATE_PAGE, to: props.page, from: currentPage});
    }
    return <ActionButton text={`➔ ${props.label}`} onClick={action} buttonClassName="btn-primary btn-sm"/>
}
