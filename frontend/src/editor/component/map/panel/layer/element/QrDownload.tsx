import QRCode from "react-qr-code";
import React, {useRef} from "react";
import {createNotification} from "../../../../../../common/util/Notification";


export const QrDownload = (props: { fileName: string, value: string }) => {

    const fileName = props.fileName.toLowerCase().replace(" ", "-")

    const qrRef = useRef<HTMLDivElement>(null);

    const createObjectURL = (svg: SVGSVGElement) => {
        const serializer = new XMLSerializer();
        const svgString = serializer.serializeToString(svg);

        const svgBlob = new Blob([svgString], {type: "image/svg+xml"});
        return URL.createObjectURL(svgBlob);
    }

    const copyPNGToClipboard = async (size: number) => {
        const svg = qrRef.current?.querySelector("svg");
        if (!svg) return;

        const url = createObjectURL(svg);

        const img = new Image();
        img.onload = async () => {
            const canvas = document.createElement("canvas");
            canvas.width = size;
            canvas.height = size;

            const ctx = canvas.getContext("2d");
            if (!ctx) return;

            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

            canvas.toBlob(async (pngBlob) => {
                if (!pngBlob) return;

                await navigator.clipboard.write([
                    new ClipboardItem({
                        "image/png": pngBlob,
                    }),
                ]);

                createNotification(undefined, "Copied to clipboard", 1000);

                URL.revokeObjectURL(url);
            }, "image/png");
        };

        img.src = url;
    };


    const downloadSVG = () => {
        const svg = qrRef.current?.querySelector("svg");
        if (!svg) return;

        const url = createObjectURL(svg);

        const a = document.createElement("a");
        a.href = url;
        a.download = `${fileName}.svg`;
        a.click();

        URL.revokeObjectURL(url);
    };

    const downloadPNG = (resolution: number) => {
        const svg = qrRef.current?.querySelector("svg");
        if (!svg) return;

        const url = createObjectURL(svg);

        const img = new Image();
        img.onload = () => {
            const canvas = document.createElement("canvas");
            canvas.width = resolution;
            canvas.height = resolution;

            const ctx = canvas.getContext("2d");
            if (!ctx) return;

            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

            const pngUrl = canvas.toDataURL("image/png");

            const a = document.createElement("a");
            a.href = pngUrl;
            a.download = `${fileName}.png`;
            a.click();

            URL.revokeObjectURL(url);
        };

        img.src = url;
    };

    return (
        <div ref={qrRef} className="row">
            <div ref={qrRef} className="col-lg-5">
                <QRCode value={props.value} size={256} viewBox="0 0 256 256"/>
            </div>
            <div ref={qrRef} className="col-lg-7">
                <br/><br/><br/><br/>
                &nbsp;&nbsp;Copy&nbsp;
                <button onClick={() => copyPNGToClipboard(128)} className="btn btn-info">small</button>
                &nbsp;
                <button onClick={() => copyPNGToClipboard(256)} className="btn btn-info">medium</button>
                &nbsp;
                <button onClick={() => copyPNGToClipboard(512)} className="btn btn-info">large</button>
                <br/><br/>
                &nbsp;&nbsp;Download&nbsp;
                <button onClick={() => downloadPNG(256)} className="btn btn-info">png</button>
                &nbsp;
                <button onClick={downloadSVG} className="btn btn-info">svg</button>
            </div>
        </div>
    );
}
