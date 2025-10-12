

const weekdayFormatter = new Intl.DateTimeFormat('en', {weekday: 'short'});
const monthFormatter = new Intl.DateTimeFormat('en', {month: 'short'});


export const formatDate = (date: Date) => {
    const day = weekdayFormatter.format(date);
    const month = monthFormatter.format(date);
    return `${day} ${date.getDate()} ${month} ${date.getFullYear()}`
}

export const formatTime = (date: Date) => {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

export const formatTimestamp = (date: Date) => {
    return `${formatDate(date)} ${formatTime(date)}`
}

