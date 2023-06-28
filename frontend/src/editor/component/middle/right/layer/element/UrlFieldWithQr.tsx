import toast, {Toast} from "react-hot-toast";
import QRCode from "react-qr-code";
import React from "react";

interface Props {
    name: string,
    url: string,
    description: string
}

export const UrlFieldWithQr = ({name, url, description}: Props) => {

    const showQR = () => {
        toast((t: Toast) => {
                const dismissMethod = () => toast.dismiss(t.id)
                return (
                    <div>
                        <div>{description}</div>
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
                               onClick={(e) => {
                                   window.open(url, "_blank")
                               }}
                               style={{textDecoration: "underline", color: "steelblue"}}
                        />
                    </span>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <div className="btn btn-info btn-sm" onClick={showQR}>QR</div>
            </div>
        </div>
    )
}