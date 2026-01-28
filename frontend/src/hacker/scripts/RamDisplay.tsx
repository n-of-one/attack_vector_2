import React from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../HackerRootReducer";
import {Ram} from "../../common/script/ScriptStatusReducer";
import {formatTimeInterval} from "../../common/util/Util";

export const RamDisplay = ({size}: {size: number}) => {
    const ram = useSelector((state: HackerRootState) => state.scriptStatus?.ram || null)
    if (ram === null) {
        return <></>
    }

    const refreshText = ram.nextRefreshSecondsLeft === null ? "" : `(next available: ${formatTimeInterval(ram.nextRefreshSecondsLeft)})`

    return <div className="text">
        <div className="row">
            <div className="col-lg-1">
                RAM
            </div>
            <div className="col-lg-10">
                <RamBar ram={ram} size={509}/>
            </div>
        </div>
        <br/>
        Total: <span className='text_light'>&nbsp; &nbsp; &nbsp;{ram.size}</span><br/>
        <br/>
        Loaded: <span className='text_light'>&nbsp; &nbsp; {ram.loaded}</span><br/>
        Refreshing: <span className='text_light'>{ram.refreshing}</span> {refreshText}<br/>
        Free: <span className='text_light'>&nbsp; &nbsp; &nbsp; {ram.free}</span><br/>
    </div>
}

export const RamBar = ({ram, size}: { ram: Ram, size: number }) => {
    if (ram.size === 0) {
        return <div className="progress-stacked">
            <div className="progress" role="progressbar" aria-label="Segment one" aria-valuenow={100} aria-valuemin={0} aria-valuemax={100}
                 style={{"width": "100%"}}>
                <div className="progress-bar bg-light-subtle dark">not installed</div>
            </div>
        </div>
    }

    const loadedPercentage = (ram.loaded / ram.size) * 100
    const refreshingPercentage = (ram.refreshing / ram.size) * 100
    const freePercentage = 100 - loadedPercentage - refreshingPercentage

    const textLoaded = textFor(loadedPercentage, size, "loaded: ", ram.loaded)
    const textRefreshing = textFor(refreshingPercentage, size, "refreshing: ", ram.refreshing)
    const textFree = textFor(freePercentage, size, " free: ", ram.free)



    return <div className="progress-stacked">
        <div className="progress" role="progressbar" aria-label="Segment one" aria-valuenow={loadedPercentage} aria-valuemin={0} aria-valuemax={100}
             style={{"width": `${loadedPercentage}%`}}>
            <div className="progress-bar bg-info">{textLoaded}</div>
        </div>
        <div className="progress" role="progressbar" aria-label="Segment two" aria-valuenow={refreshingPercentage} aria-valuemin={0}
             aria-valuemax={100}
             style={{"width": `${refreshingPercentage}%`}}>
            <div className="progress-bar bg-primary-subtle">{textRefreshing}</div>
        </div>
        <div className="progress" role="progressbar" aria-label="Segment three" aria-valuenow={freePercentage} aria-valuemin={0} aria-valuemax={100}
             style={{"width": `${freePercentage}%`}}>
            <div className="progress-bar bg-light-subtle">{textFree}</div>
        </div>
    </div>
}

const textFor = (percentage: number, barSize: number, prefix: string, amount: number) => {
    const sizeAvailable = (barSize * percentage) / 100
    const fullText = `${prefix}${amount}`
    const PADDING = 2
    const sizeNeeded = (fullText.length + PADDING) * 7.3
    if (sizeAvailable > sizeNeeded) {
        return fullText
    }
    return amount.toString()
}
