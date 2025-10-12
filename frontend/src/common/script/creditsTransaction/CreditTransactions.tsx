import {DataTable} from "../../component/dataTable/DataTable";
import React from "react";
import {CreditTransaction} from "./CreditTransactionReducer";
import {formatTimestamp} from "../../util/TimeUtil";
import {Hr} from "../../component/dataTable/Hr";


export const CreditTransactions = ({transactions, viewForUserName}: { transactions: CreditTransaction[], viewForUserName: string }) => {
    return <>
        <h4>
            Transactions
        </h4>
        <hr/>
        <div>
            <ScriptsTransactionsTable transactions={transactions} viewForUserName={viewForUserName}/>
        </div>
    </>
}

const ScriptsTransactionsTable = ({transactions, viewForUserName}: { transactions: CreditTransaction[], viewForUserName: string }) => {
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

    return <DataTable rows={rows} rowTexts={rowTexts} pageSize={35} hr={<Hr/>}>
        <div className="row text strong">
            <div className="col-lg-2">Timestamp</div>
            <div className="col-lg-1 text-end">Amount</div>
            <div className="col-lg-2">Other</div>
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
        <div className="col-lg-2">{formatTimestamp(new Date(transaction.timestamp))}</div>
        <div className="col-lg-1 text-end">{sign + transaction.amount} ⚡</div>
        <div className={`col-lg-2 ${otherUserStyle}`}>{otherUserName}</div>
        <div className="col-lg-4">{transaction.description}</div>
    </div>
}
