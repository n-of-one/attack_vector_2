import React, {useEffect} from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../HackerRootReducer";
import {HackerSkillType} from "../../common/users/HackerSkills";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {ScriptIncomeCollectionStatus} from "../../common/users/CurrentUserReducer";
import {ActionButton} from "../../common/component/ActionButton";
import {notifySimple} from "../../common/util/Notification";
import {TwoTextInput} from "../../common/component/TwoTextInput";
import {InfoBadge} from "../../common/component/ToolTip";
import {CreditTransactions} from "../../common/script/creditsTransaction/CreditTransactions";

export const HackerScriptCredits = () => {

    useEffect(() => {
        webSocketConnection.send("/hacker/creditTransaction/send", null)
    }, [])


    const transactions = useSelector((state: HackerRootState) => state.creditTransactions) || []
    const currentUserName = useSelector((state: HackerRootState) => state.currentUser).name

    return (
        <div className="row content">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-4 text">
                <br/>
                <strong>🜁 Verdant OS 🜃</strong><br/>
                <br/>
                <h3 className="text-info">Credits</h3>
                <br/>
                <CreditsAndIncome/>
                <ScriptIncome/>
                <TransferCredits/>
                <hr/>
            </div>
            <div className="col-lg-7">
                <div className="rightPanel">
                    <CreditTransactions transactions={transactions} viewForUserName={currentUserName}/>
                </div>
            </div>
        </div>
    )
}

const CreditsAndIncome = () => {
    const credits = useSelector((state: HackerRootState) => state.currentUser.hacker?.scriptCredits) || 0

    return <>
                Script credit balance: <span className="text-info">{credits} <span className="glyphicon glyphicon-flash"/></span>
        <br/>
        <hr/>
    </>
}

const ScriptIncome = () => {
    const currentUser = useSelector((state: HackerRootState) => state.currentUser)
    if (!currentUser.hacker) {
        return <></>
    }

    const incomeStatus = currentUser.hacker?.scriptIncomeCollectionStatus
    const skills = currentUser.hacker.skills
    const scriptCreditsSkill = skills.find((hackerSKill) => hackerSKill.type === HackerSkillType.SCRIPT_CREDITS)
    const income = parseInt(scriptCreditsSkill?.value || "0")

    if (incomeStatus === ScriptIncomeCollectionStatus.HACKER_HAS_NO_INCOME || income === 0) {
        return <></>
    }

    const collectIncome = () => {
        webSocketConnection.send("/hacker/scriptCredits/collect", null)
    }
    const collectionLink = (incomeStatus === ScriptIncomeCollectionStatus.AVAILABLE) ?
        <><br/><br/><ActionButton onClick={collectIncome} text="Collect income"/></> : <></>
    const incomeInfo = <>Income <span className="text-info">{income} <span className="glyphicon glyphicon-flash"/></span></>

    return <>
        {incomeInfo} <IncomeStatusElement incomeStatus={incomeStatus}/> {collectionLink}
        <br/><br/>
        <hr/>
    </>
}

const IncomeStatusElement = ({incomeStatus} : {incomeStatus: ScriptIncomeCollectionStatus}) => {
    switch (incomeStatus) {
        case ScriptIncomeCollectionStatus.AVAILABLE: return <span className="badge bg-primary">Available</span>
        case ScriptIncomeCollectionStatus.TODAY_IS_NOT_AN_INCOME_DATE: return <>
            <span className="badge bg-secondary">Today is not an income date</span> &nbsp;
            <InfoBadge infoText="If you think that you should be getting income today, please contact a GM" />
        </>
        case ScriptIncomeCollectionStatus.COLLECTED: return <span className="badge bg-success">Collected</span>
        default:  return <span>You have no script income</span>
    }
}

const TransferCredits = () => {
    const transfer = (amount: string, receiver: string) => {
        if (!receiver || !amount) {
            return
        }
        if (!isWholePositiveNumber(amount)) {
            notifySimple("Please enter a positive whole number for the amount.")
        }
        webSocketConnection.send("/hacker/scriptCredits/transfer", {receiver, amount})
    }
    return <>
        <TwoTextInput label="Transfer credits" save={transfer} clearAfterSubmit={false} buttonLabel="Transfer" buttonClass="btn btn-info"
                      placeholder1="amount" placeholder2="reciever" autofocus={false} size={3} labelColumns={3}/>
    </>
}

const isWholePositiveNumber = (amount: string): boolean => {
    const parsed = parseInt(amount)
    return !isNaN(parsed) && parsed > 0 && parsed.toString() === amount
}

