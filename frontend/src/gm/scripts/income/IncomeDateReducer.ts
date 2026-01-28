import {AnyAction} from "redux";

export enum IncomeDateStatus {
    PAST = 'PAST',
    COLLECTABLE = 'COLLECTABLE',
    SCHEDULED = 'SCHEDULED'
}

export interface IncomeDate {
    id: string,
    date: Date,
    status: IncomeDateStatus
    collectedByUserNames: string[]
}

const SERVER_RECEIVE_INCOME_DATES = "SERVER_RECEIVE_INCOME_DATES"


export const incomeDateReducer = (state: IncomeDate[] = [], action: AnyAction): IncomeDate[] => {

    switch (action.type) {
        case SERVER_RECEIVE_INCOME_DATES:
            return parseIncomeDates(action.data)
        default:
            return state
    }
}

interface ServerIncomeDate {
    id: string,
    date: string,
    status: IncomeDateStatus
    collectedByUserNames: string[]
}

function parseIncomeDates(incomeDates: ServerIncomeDate[]): IncomeDate[] {
    return incomeDates.map((serverIncomeDate: ServerIncomeDate) => {
        return {
            ...serverIncomeDate,
            date: new Date(serverIncomeDate.date)
        }
    })
}
