import React, {useEffect} from "react";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {CLOSE_USER_EDIT, UserOverview} from "../../../common/users/EditUserDataReducer";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {UserOverviewTable} from "../../../common/users/UserManagement";
import {ScriptsTransactionsTable} from "../../../common/script/creditsTransaction/CreditTransactions";
import {CreditTransaction} from "../../../common/script/creditsTransaction/CreditTransactionReducer";
import {CloseButton} from "../../../common/component/CloseButton";
import {User} from "../../../common/users/CurrentUserReducer";

export const GmCreditTransactions = () => {

    useEffect(() => {
        webSocketConnection.send("/user/overview", "")
    }, [])

    const transactions = useSelector((state: GmRootState) => state.creditTransactions) || []

    const userOverViewLines: UserOverview[] = useSelector((state: GmRootState) => state.users.overview)
    const hackers = userOverViewLines.filter(overViewLine => overViewLine.hacker)

    const user = useSelector((state: GmRootState) => state.users.edit.userData)
    const transactionsElement = user ? <TransactionsOfUser transactions={transactions} user={user}/> :
        <div className="text">Click on a user in the table on the right to manage their credit transactions.</div>


    const selectUser = (userOverview: UserOverview) => {
        webSocketConnection.send("/user/select", userOverview.id)
        webSocketConnection.send("/gm/creditTransaction/send", userOverview.id)
    }


    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Manage current scripts of hackers</h3><br/>
                </div>
                {transactionsElement}
            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                    <UserOverviewTable users={hackers} selectUser={selectUser}/>
                </div>
            </div>
        </div>
    )
}


const TransactionsOfUser = ({user, transactions}: { user: User, transactions: CreditTransaction[] }) => {

    const dispatch = useDispatch()
    const close = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    return <div>
        <div className="d-flex justify-content-between text">
            <h5 className="text-muted">Transaction history of <span className="text_gold">{user.name}</span></h5>
            <h5><CloseButton closeAction={close}/></h5>
        </div>
        <ScriptsTransactionsTable transactions={transactions} viewForUserName={user.name}/>
        <br/>
    </div>
}
