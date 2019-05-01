import React from 'react';
import {connect} from "react-redux";

/**
 * This goal of this component is to prevent the displaying of the link URL over the menu bar at the bottom of the screen.
 */

/* eslint jsx-a11y/alt-text: 0*/

const mapDispatchToProps = () => {
    return {}
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({href, classNameInput, children, onClick}) => {

        const onClickLocal = (e) => {
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
        return ( <a onClick={(e) => onClickLocal(e) } className={className}>{children}</a> )
    });