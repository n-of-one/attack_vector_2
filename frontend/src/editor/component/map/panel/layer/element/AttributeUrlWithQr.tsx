import toast, {Toast} from "react-hot-toast";
import QRCode from "react-qr-code";
import React from "react";
import {avEncodedUrl} from "../../../../../../common/util/PathEncodeUtils";

type UrlType = "ice" | "app" | "widget"

interface Props {
    name: string,
    type: UrlType,
    subType?: string,
    layerId: string,
    description: string,
    requireLogin?: boolean
}

export const AttributeUrlWithQr = ({name, layerId, type, subType, description, requireLogin = true}: Props) => {

    const path = createPath(layerId, type, subType)
    const url = avEncodedUrl(path, requireLogin)

    const showQR = () => {
        toast((t: Toast) => {
                const dismissMethod = () => toast.dismiss(t.id)
                return (
                    <div>
                        <div>{description}</div>
                        <div>&nbsp;</div>
                        <div>Path: {path}</div>
                        <div>&nbsp;</div>
                        <div>{url}</div>
                        <div onClick={dismissMethod}><br/>
                            <QRCode size={256} value={url} viewBox="0 0 256 256"/>
                            <div>&nbsp;</div>
                            <div>Click to close</div>
                        </div>
                    </div>)
            },
            {
                duration: 0,
                style: {
                    borderRadius: '5px', background: '#fff', color: '#333', border: '2px solid #000', maxWidth: '900px',
                },
            })
    }

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{name}</div>
            <div className="col-lg-8 noRightPadding">
                    <span>
                        <input type="text" className="form-control input-sm" readOnly={true}
                               value={url}
                               // onClick={(e) => {
                               //     window.open(url, "_blank")
                               // }}
                               // style={{textDecoration: "underline", color: "steelblue"}}
                        />
                    </span>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <div className="btn btn-info btn-sm" onClick={showQR}>QR</div>
            </div>
        </div>
    )
}

const createPath = (layerId: string, type: UrlType, subType?: string): string => {
    switch(type) {
        case "ice": return `ice/${layerId}`
        case "app": return `app/${subType}/${layerId}`
        case "widget": return `widget/${subType}/${layerId}`
        default: return `Unknown type: ${type}`
    }
}