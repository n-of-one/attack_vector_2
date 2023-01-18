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
    children?: JSX.Element,
    onClick?: (e: MouseEvent) => void,
    title?: string
}

const SilentLink = ({href, classNameInput, children, onClick, title}: Props) => {
    if (!classNameInput) classNameInput = '';

    const onClickLocal = (e: any) => {
        if (onClick) {
            onClick(e);
            return;
        }
        if (href) {
            window.location.href = href;
        }
    };

    const className = "silentLink " + classNameInput;

    // eslint-disable-next-line
    return (<a onClick={(e) => onClickLocal(e)} className={className} title={title}>{children}</a>)
};

export default SilentLink
