import React from "react";

interface Props {
    path: string
    alt?: string
    width?: string
}

export const WebsiteImage = ({path, alt = "", width=""}: Props) => {
    return <a href={path}><img src={path} className="img-fluid" alt={alt} width={width}/></a>
}

