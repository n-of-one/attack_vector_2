import React from 'react';

/**
 * This goal of this component is to prevent the displaying of the link URL over the menu bar at the bottom of the screen.
 *
 * Props:
 * - className
 * - href     (choose one)
 * - onClick  (choose one)
 * - children (implicit)
 * - title
 */

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    href?: string,
    classNameInput?: string,
    children?: React.JSX.Element,
    onClick?: (e: MouseEvent) => void,
    title?: string,
    text?: string,
    newTab?: boolean,
}

export const SilentLink = ({href, classNameInput, children, onClick, title, text, newTab}: Props) => {
    if (!classNameInput) classNameInput = '';

    const onClickLocal = (e: any) => {
        if (onClick) {
            onClick(e);
            return;
        }
        if (href) {
            if (newTab) {
                window.open(href, '_blank');
            } else {
                window.location.href = href;
            }
        }
    };

    const className = "silentLink " + classNameInput;

    const inner = text ? text : children;

    // eslint-disable-next-line
    return (<a onClick={(e) => onClickLocal(e)} className={className} title={title}>{inner}</a>)
};
