import React from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../HackerRootReducer";
import {SilentLink} from "../../common/component/SilentLink";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {ScriptAccess} from "../../common/script/access/ScriptAccessReducer";
import {Hr} from "../../common/component/dataTable/Hr";
import {DataTable} from "../../common/component/dataTable/DataTable";
import {ScriptEffects} from "../../common/script/type/ScriptEffects";
import {HackerScriptsPanel} from "../scripts/HackerScriptsPanel";
import {Pad} from "../../common/component/Pad";


export const HackerScriptMarket = () => {

    return (
        <div className="row content">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-4 text">
                <br/>
                <strong>🜁 Verdant OS 🜃</strong><br/>
                <br/>
                <h3 className="text-info">Script Market</h3>
                <br/>
                <hr/>
                <MarketScriptsList/>
            </div>
            <div className="col-lg-7">
                <div className="rightPanel">
                    <HackerScriptsPanel/>
                </div>
            </div>
        </div>
    )
}

export const MarketScriptsList = () => {
    const accesses = useSelector((state: HackerRootState) => state.scriptAccess).filter((access: ScriptAccess) => access.price !== null && access.price > 0)
    accesses.sort((a, b) => a.type.name.localeCompare(b.type.name))
    const credits = useSelector((state: HackerRootState) => state.currentUser.hacker?.scriptCredits) || 0

    if (accesses.length === 0) {
        return <div className="text">No scripts available in the market.</div>
    }

    const maxPricePadding = accesses.reduce((max, access) => {
        return Math.max(max, pricePadding(access.price))
    }, 0)


    const rows = accesses.map((scriptAccess: ScriptAccess) => {
        return <MarketScriptLine scriptAccess={scriptAccess} creditsAvailable={credits} maxPricePadding={maxPricePadding}/>
    })
    const rowTexts = accesses.map((access: ScriptAccess) => `${access.type.name}~${access.type.size}~${access.price}`)


    return <>
        Scripts available on the market.<br/>
        <br/><span className="dark">These scripts will expire the same as other hacker scripts. Buy them if you want to use them today.</span><br/>
        <br/>
        <br/>
        <DataTable rows={rows} rowTexts={rowTexts} pageSize={35} hr={<Hr/>}>
            <div className="row text strong">
                <div className="col-lg-4">Name</div>
                <div className="col-lg-2">RAM</div>
                <div className="col-lg-2">Effects</div>
                <div className="col-lg-2">Price</div>
                <div className="col-lg-2">Action</div>
            </div>
        </DataTable>
    </>

}

interface MarketScriptLineProps {
    scriptAccess: ScriptAccess,
    creditsAvailable: number,
    maxPricePadding: number
}

const MarketScriptLine = ({scriptAccess, creditsAvailable, maxPricePadding}: MarketScriptLineProps) => {

    const padding = maxPricePadding - pricePadding(scriptAccess.price)

    return (<>
            <div className="row text" style={{marginBottom: "2px"}}>
                <div className="col-lg-4">{scriptAccess.type.name}</div>
                <div className="col-lg-2">{scriptAccess.type.size}</div>
                <div className="col-lg-2"><ScriptEffects effects={scriptAccess.type.effects}/></div>
                <div className="col-lg-2"><Pad length={padding}/>{scriptAccess.price} <span className="glyphicon glyphicon-flash"/></div>
                <div className="col-lg-2 noSelect">
                    <ActionBuyScript scriptAccess={scriptAccess} creditsAvailable={creditsAvailable}/>
                </div>
            </div>
        </>
    )
}

const ActionBuyScript = ({scriptAccess, creditsAvailable}: { scriptAccess: ScriptAccess, creditsAvailable: number }) => {
    if (creditsAvailable < scriptAccess.price!!) {
        return <></>
    }

    const action = () => {
        webSocketConnection.send("/hacker/script/buy", scriptAccess.id)
    }
    return <SilentLink onClick={action}><>buy</>
    </SilentLink>
}


const pricePadding = (price: number | null): number => {
    return Math.floor(Math.log10(price!!));
}
