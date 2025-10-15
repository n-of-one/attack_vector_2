import {AnyAction} from "redux";

export interface CreditTransaction {
    id: string,
    fromUserName: string,
    fromUserType: string,
    toUserName: string,
    toUserType: string,
    timestamp: string,
    amount: number,
    description: string,
}

const SERVER_RECEIVE_CREDITS_TRANSACTIONS = "SERVER_RECEIVE_CREDITS_TRANSACTIONS"

export const creditTransactionReducer = (state: CreditTransaction[] = [], action: AnyAction): CreditTransaction[] => {
    switch (action.type) {
        case SERVER_RECEIVE_CREDITS_TRANSACTIONS:
            return action.data
        default:
            return state
    }
}
