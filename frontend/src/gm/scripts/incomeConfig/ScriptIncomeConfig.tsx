import React, {useState} from "react";
import {DateInput} from "../../../common/component/DateInput";


export const ScriptIncomeConfig = () => {
    const [startDate, setStartDate] = useState(new Date());
    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Script income configuration </h3><br/>
                    Hackers that have script credits income, will receive this every day. However, you usually don't want
                    hackers to receive that income outside of the days that the larp is running.<br/>
                    <br/>
                    <br/>
                    <DateInput selectedDate={startDate} onChange={(date) => {
                        if (date != null) setStartDate(date)
                    }
                    }/>
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
