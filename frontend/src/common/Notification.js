
const iziToast = require("izitoast");

const notify_ok = (title, message, position) => {
    iziToast.show({
        title: title,
        message: message,
        position: position,
        color: 'green',
    });
};

const notify_neutral = (title, message) => {
    iziToast.show({
        title: title,
        message: message,
        position: 'topCenter',
        color: 'yellow'
    });
};

const notify_error = (title, message) => {
    iziToast.show({
        title: title,
        message: message,
        position: 'topCenter',
        color: 'blue',
    });
};

const notify_fatal = (message) => {
    iziToast.show({
        title: 'Fatal',
        message: message,
        position: 'topCenter',
        color: 'red',
        timeout: false
    });
};


const notify_advice = (title, message, position) => {
    iziToast.show({
        title: title,
        message: message,
        position: position,
        color: 'green',
        timeout: false
    });
};

const notify = ({type, title, message}) => {

    if (type === "ok") {
        notify_ok(title, message, "topCenter");
    }
    if (type === "ok_right") {
        notify_ok(title, message, "topRight");
    }
    else  if (type === "advice_right") {
        notify_advice(title, message, "topRight");
    }
    else if (type === "advice_left") {
        notify_neutral(title, message, "topLeft");
    }
    else if (type === "neutral") {
        notify_neutral(title, message);
    }
    else  if (type === "error") {
        notify_error(title, message);
    }
    else {
        notify_fatal(title + " " + message);
    }
};

export { notify, notify_neutral, notify_fatal }