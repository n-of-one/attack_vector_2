import React, {useEffect, useState} from "react";
import DatePicker from "react-datepicker";
import 'react-datepicker/dist/react-datepicker.css';
import {ActionButton} from "../../../common/component/ActionButton";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {SilentLink} from "../../../common/component/SilentLink";
import {Hr} from "../../../common/component/dataTable/Hr";
import {IncomeDate, IncomeDateStatus} from "./IncomeDateReducer";
import {useSelector} from "react-redux";
import {GmRootState} from "../../GmRootReducer";
import {InfoBadge} from "../../../common/component/ToolTip";

export const ScriptIncome = () => {

    useEffect(() => {
        webSocketConnection.send("/gm/scriptIncome/sendDates", null)
    }, [])

    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Script income dates </h3><br/>
                    Hackers that have a daily script credits income, can collect it during the days of the Larp. Here you define the
                    days hackers can collect income (i.e. the days of your larp).<br/>
                    <br/>
                    Income dates start at 06:00 at the specified date, and end at 06:00 the next day.
                    This way, the hours past midnight of a specific day count as the same income day. This is the same with script expiry and
                    daily script access.<br/>
                    <br/>
                    <h4 className="text-secondary">Add dates </h4><br/>
                    <AddSingleDate/>
                    <br/>
                    <AddDateRange/>
                    <br/>
                    <br/>
                    <IncomeDates/>
                </div>
            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                    &nbsp;
                </div>
            </div>
        </div>
    )
}

const IncomeDates = () => {
    const incomeDates = useSelector((state: GmRootState) => state.incomeDates).sort((a, b) => a.date.getMilliseconds() - b.date.getMilliseconds())


    return <>
        <h4 className="text-secondary">Income Dates</h4>

        <IncomeDateHeaders/>
        {incomeDates.map((date) => {
                return <IncomeDateLine key={date.id} incomeDate={date}/>
            }
        )}
    </>
}

const incomeDateStatusToBadgeClass: Record<IncomeDateStatus, string> = {
    [IncomeDateStatus.PAST]: "text-bg-secondary",
    [IncomeDateStatus.COLLECTABLE]: "text-bg-success",
    [IncomeDateStatus.SCHEDULED]: "text-bg-secondary"

}

const IncomeDateHeaders= () => {
    return <div className="row">
        <div className="col-lg-3">
            Date
        </div>
        <div className="col-lg-3">
            Time
        </div>
        <div className="col-lg-2">
            Status
        </div>
        <div className="col-lg-2">
            Collected
        </div>
        <div className="col-lg-1">
            Action
        </div>
        <Hr/>
    </div>
}

const IncomeDateLine = ({incomeDate}: { incomeDate: IncomeDate }) => {

    const day = new Intl.DateTimeFormat('en', {weekday: 'short'}).format(incomeDate.date);
    const month = new Intl.DateTimeFormat('en', {month: 'short'}).format(incomeDate.date);
    const dateText = `${day} ${incomeDate.date.getDate()} ${month} ${incomeDate.date.getFullYear()}`

    const statusBadgeClass = incomeDateStatusToBadgeClass[incomeDate.status]

    const collectionCount = incomeDate.collectedByUserNames.length
    const collectedByText = createCollectedByText(incomeDate.status, incomeDate.collectedByUserNames)

    const collectedByBadge = (incomeDate.status !== IncomeDateStatus.SCHEDULED) ? <InfoBadge infoText={collectedByText} badgeText={`${collectionCount}x`}/> : null

    return <div className="row">
        <div className="col-lg-3">
            {dateText}
        </div>
        <div className="col-lg-3">
            <span className="dark">start at 06:00</span>
        </div>
        <div className="col-lg-2">
            <span className={`badge ${statusBadgeClass}`}>{`${incomeDate.status}`}</span>
        </div>
        <div className="col-lg-2">
            <span>{collectedByBadge}</span>
        </div>
        <div className="col-lg-2">
            <SilentLink onClick={() => deleteDate(incomeDate.id)} title="Remove">
                <span className="glyphicon glyphicon-trash"/>
            </SilentLink>&nbsp;&nbsp;
        </div>
        <Hr/>
    </div>
}

const createCollectedByText = (status: IncomeDateStatus, collectedByUserNames: string[]): string => {
    const collectionCount = collectedByUserNames.length
    const names = collectedByUserNames.join(", ")

    switch(status) {
        case IncomeDateStatus.PAST: return (collectionCount > 0 ) ? `Collected by: ${names}` : "No collections"
        case IncomeDateStatus.COLLECTABLE: return (collectionCount > 0 ) ? `Collected by: ${names}` : "Not collected yet"
        default: return ""
    }
}

const AddSingleDate = () => {
    const [date, setDate] = useState(new Date());
    return <div className="row">
        <div className="col-lg-2">
            <ActionButton text="Add date" onClick={() => addSingleDate(date)}/>
        </div>
        <div className="col-lg-2">
            <DatePicker
                selected={date}
                onChange={(date) => {
                    if (date != null) setDate(date);
                }}
                dateFormat="dd-MMM-yyyy"
                placeholderText="Select a date"
                className="form-control"
            />
        </div>
    </div>
}

const AddDateRange = () => {
    const [startDate, setStartDate] = useState(new Date());
    const [endDate, setEndDate] = useState(new Date());
    return <div className="row">
        <div className="col-lg-2">
            <ActionButton text="Add range" onClick={() => addDateRange(startDate, endDate)}/>
        </div>
        <div className="col-lg-2">
            <DatePicker
                selected={startDate}
                onChange={(date) => {
                    if (date != null) setStartDate(date);
                }}
                dateFormat="dd-MMM-yyyy"
                placeholderText="Select a date"
                className="form-control"
            />
        </div>
        <div className="col-lg-1" style={{width: "20px"}}>
            <span style={{top: "8px", position: "relative"}}>-</span>
        </div>

        <div className="col-lg-2">
            <DatePicker
                selected={endDate}
                onChange={(date) => {
                    if (date != null) setEndDate(date);
                }}
                dateFormat="dd-MMM-yyyy"
                placeholderText="Select a date"
                className="form-control"
            />
        </div>
    </div>
}


const addSingleDate = (date: Date) => {
    const isoDate = date.toISOString().substring(0, 10)
    webSocketConnection.send("/gm/scriptIncome/add", {start: isoDate, end: isoDate})
}

const addDateRange = (startDate: Date, endDate: Date) => {
    const start = startDate.toISOString().substring(0, 10)
    const end = endDate.toISOString().substring(0, 10)
    webSocketConnection.send("/gm/scriptIncome/add", {start, end})
}

const deleteDate = (id: string) => {
    webSocketConnection.send("/gm/scriptIncome/delete", id)
}
