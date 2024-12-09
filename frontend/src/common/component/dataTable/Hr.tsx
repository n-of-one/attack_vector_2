import React from "react";

interface Props {
    height?: number
    color?: string,
    marginTop?: number
}

export const Hr = (props: Props) => {
    const height = props.height ? props.height : 4
    const color = props.color ? props.color : "black"
    const marginTop = props.marginTop ? props.marginTop : 0

    return (
        <div className="row" style={{height: height}}>
            <div className="col-lg-12">
                <hr style={{color: color, margin: `${marginTop}px -4px 0 -4px`}}/>
            </div>
        </div>
    )

}
