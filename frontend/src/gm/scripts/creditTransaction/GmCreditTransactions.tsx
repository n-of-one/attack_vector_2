import React, {useEffect} from "react";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {CLOSE_USER_EDIT, UserOverview} from "../../../common/users/EditUserDataReducer";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {UserOverviewTable} from "../../../common/users/UserManagement";
import {ScriptsTransactionsTable} from "../../../common/script/credits/CreditTransactions";
import {CloseButton} from "../../../common/component/CloseButton";
import {User} from "../../../common/users/CurrentUserReducer";
import {notifySimple} from "../../../common/util/Notification";
import {TwoTextInput} from "../../../common/component/TwoTextInput";
import {CreditsIcon} from "../../../common/component/icon/CreditsIcon";
import {CurrentCredits} from "../../../common/script/credits/CurrentCredits";

export const GmCreditTransactions = () => {

    useEffect(() => {
        webSocketConnection.send("/user/overview", "")
    }, [])


    const userOverViewLines: UserOverview[] = useSelector((state: GmRootState) => state.users.overview)
    const hackers = userOverViewLines.filter(overViewLine => overViewLine.hacker)

    const user = useSelector((state: GmRootState) => state.users.edit.userData)
    const hackerCreditsElement = user ? <HackerCreditsElement user={user}/> :
        <div className="text">Click on a user in the table on the right to manage their credit transactions.</div>


    const selectUser = (userOverview: UserOverview) => {
        webSocketConnection.send("/user/select", userOverview.id)
        webSocketConnection.send("/gm/creditTransaction/send", userOverview.id)
    }


    return (
        <div className="row text">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div>
                    <h3 className="text-info">Manage credits of hacker</h3>
                </div>
                {hackerCreditsElement}
            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                    <UserOverviewTable users={hackers} selectUser={selectUser}/>
                </div>
            </div>
        </div>
    )
}

const HackerCreditsElement = ({user}: { user: User }) => {
    const dispatch = useDispatch()
    const close = () => {
        dispatch({type: CLOSE_USER_EDIT})
    }

    return <>
        <div className="d-flex justify-content-between text">
            <h5 className="text-muted">Credits and transaction history of <span className="text_gold">{user.name}</span></h5>
            <h5><CloseButton closeAction={close}/></h5>
        </div>
        <hr/>
        <CurrentCredits user={user}/>
        <hr/>
        <AdjustCredits user={user}/>
        <hr/>
        <TransactionsOfUser user={user}/>

    </>
}


const TransactionsOfUser = ({user}: { user: User }) => {

    const transactions = useSelector((state: GmRootState) => state.creditTransactions) || []

    return <div>
        <ScriptsTransactionsTable transactions={transactions} viewForUserName={user.name} pageSize={30}/>
        <br/>
    </div>
}

const AdjustCredits = ({user}: { user: User }) => {
    const transfer = (amount: string, description: string) => {
        if (!isWholeNumber(amount)) {
            notifySimple("Please enter a whole number for the amount.")
            return
        }
        if (!description) {
            notifySimple("Please provide the description. This will show up in the transaction of the hacker.")
            return
        }
        webSocketConnection.send("/gm/scriptCredits/adjust", {userId: user.id, amount, description})
    }
    return <>
        <TwoTextInput label="Adjust credits" save={transfer} clearAfterSubmit={false} buttonLabel="Adjust" buttonClass="btn btn-info"
                      placeholder1="amount, + or -" placeholder2="description" autofocus={false} size={3} labelColumns={3}
                      type1="number"/>
    </>
}

const isWholeNumber = (amount: string): boolean => {
    const parsed = parseInt(amount)
    return !isNaN(parsed) && parsed.toString() === amount
}
