import React from 'react';

const sameDay = (date, year, month, day) => {
    return (day === date.getDate() &&
        month === date.getMonth() &&
        year === date.getFullYear());
};

const renderDate = (timestamp) => {
    const day = timestamp.getDate();
    const month = timestamp.getMonth();
    const year = timestamp.getFullYear();

    const today = new Date();
    if (sameDay(today, year, month, day )) {
        return "today"
    }
    const yesterday = today - 1;
    if (sameDay(yesterday, year, month, day )) {
        return "yesterday"
    }
    return "" + year + "-" + month + "-" + day;
};

const TimeStamp = ({timestamp}) => {

    const now = new Date();
    const diffMs = (now - timestamp);
    const diffMins = Math.round(((diffMs % 86400000) % 3600000) / 60000); // minutes
    if (diffMins < 60) {
        return <span>{diffMins + " minutes ago"}</span>
    }

    const hours = timestamp.getHours();
    const minutes = timestamp.getMinutes();

    return <span>{renderDate(timestamp)} {hours}:{minutes}</span>
};

export default TimeStamp;
