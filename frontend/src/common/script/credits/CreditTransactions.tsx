import {DataTable} from "../../component/dataTable/DataTable";
import React from "react";
import {CreditTransaction} from "./CreditTransactionReducer";
import {formatTimestamp} from "../../util/TimeUtil";
import {Hr} from "../../component/dataTable/Hr";
import {CreditsIcon} from "../../component/icon/CreditsIcon";

interface Props {
    transactions: CreditTransaction[],
    viewForUserName: string,
    pageSize?: number
}

export const ScriptsTransactionsTable = ({transactions, viewForUserName, pageSize}: Props) => {
    if (transactions.length === 0) {
        return <div className="text">No transactions found.</div>
    }

    const sortedTransactions = sortTransactions([...transactions])

    const rows = sortedTransactions.map((transaction: CreditTransaction) => {
        return <TransactionLine transaction={transaction} viewForUserName={viewForUserName}/>
    })
    const rowTexts = sortedTransactions.map((transaction: CreditTransaction) =>
        `${formatTimestamp(new Date(transaction.timestamp))}~${transaction.amount}~${transaction.fromUserName}~${transaction.toUserName}~${transaction.description}`
    )

    const pageSizeValue = pageSize || 35

    return <DataTable rows={rows} rowTexts={rowTexts} pageSize={pageSizeValue} hr={<Hr/>}>
        <div className="row text strong">
            <div className="col-lg-5">
                    <div className="d-flex justify-content-between">
                        <div>Timestamp</div>
                        <div>Amount</div>
                    </div>
            </div>
            <div className="col-lg-3">Sender/Receiver</div>
            <div className="col-lg-4">Description</div>
        </div>
    </DataTable>
}

const sortTransactions = (transactions: CreditTransaction[]): CreditTransaction[] => {
    return transactions.sort((a: CreditTransaction, b: CreditTransaction) => {
        return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    })
}

const TransactionLine = ({transaction, viewForUserName}: { transaction: CreditTransaction, viewForUserName: string }) => {
    const receiving = (transaction.toUserName === viewForUserName)
    const sign = (receiving) ? "" : "-"
    const otherUserName = (receiving) ? transaction.fromUserName : transaction.toUserName
    const otherUserType = (receiving) ? transaction.fromUserType : transaction.toUserType
    const otherUserIsSystem = otherUserType === "SYSTEM"

    const otherUserStyle = otherUserIsSystem ? " dark" : ""

    return <div className="row text">
        <div className="col-lg-5">
            <div className="d-flex justify-content-between">
                <div>{formatTimestamp(new Date(transaction.timestamp))}</div>
                <div>{sign + transaction.amount}<CreditsIcon/></div>
            </div>
        </div>
        <div className={`col-lg-3 ${otherUserStyle}`}>{otherUserName}</div>
        <div className="col-lg-4">{transaction.description}</div>
    </div>
}
