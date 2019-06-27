import Schedule from "./Schedule";

/**
 * This class represents a collection of schedules, used by different actors.
 * It is used to allow multiple actions simultaneously.
 */

export default class Schedules {

    schedulesById = {};

    getOrCreateSchedule(id) {
        if (this.schedulesById[id]) {
            return this.schedulesById[id];
        }
        const newSchedule = new Schedule();
        this.schedulesById[id] = newSchedule;
        return newSchedule
    }

    deactivate() {
        Object.keys(this.schedulesById).forEach(id => {
            this.schedulesById[id].deactivate();
        });
    }

}