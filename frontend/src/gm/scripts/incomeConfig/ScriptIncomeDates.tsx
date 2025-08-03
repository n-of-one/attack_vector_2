import React, {useState} from "react";
import DatePicker from "react-datepicker";
import 'react-datepicker/dist/react-datepicker.css';
import {ActionButton} from "../../../common/component/ActionButton";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {SilentLink} from "../../../common/component/SilentLink";
import {Hr} from "../../../common/component/dataTable/Hr";

export const ScriptIncomeDates = () => {
    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Script income dates </h3><br/>
                    Hackers that have a daily script credits income, should receive it every day. Here you define the
                    days your larp runs on. If the hackers have access to Attack Vector outside of the larp, they can't
                    gain income outside of the larp.<br/>
                    <br/>
                    Define what dates the hackers will receive income.<br/>
                    <br/>
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
                    right panel
                </div>
            </div>
        </div>
    )
}

const IncomeDates = () => {

    const dates = [new Date(), new Date(), new Date()]
    return <>
        <h4 className="text-info">Income Dates</h4>

        <Hr/>

        {dates.map((date) => {
                const dateKey = `${date}`
                return <IncomeDateLine key={dateKey} date={date}/>
            }
        )}
    </>
}

const IncomeDateLine = ({date}: { date: Date }) => {

    const day = new Intl.DateTimeFormat('en', {weekday: 'short'}).format(date);
    const month = new Intl.DateTimeFormat('en', {month: 'short'}).format(date);
    const dateText = `${day} ${date.getDate()} ${month} ${date.getFullYear()}`

    const deleteScan = () => {
        webSocketConnection.send("/run/deleteRunLink", null)
    }

    return <div className="row">
        <div className="col-lg-12">
            {dateText} <span className="dark"> payout at 04:00</span>&nbsp;
            <SilentLink onClick={deleteScan} title="Remove">
                <span className="glyphicon glyphicon-trash"/>
            </SilentLink>
        </div>
        <Hr/>
    </div>
}

const AddSingleDate = () => {
    const [date, setDate] = useState(new Date());
    return <div className="row">
        <div className="col-lg-2">
            <ActionButton text="Add date" onClick={() => setDate(date)}/>
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
            <ActionButton text="Add range" onClick={() => setStartDate(startDate)}/>
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
