import Thread from "./Thread";

/**
 * This class represents a collection of threads, used by different actors.
 * It is used to allow multiple actions simultaneously.
 */

export default class Threads {

    threadsById = {};

    run(id, wait, functionToRun) {
        this.getOrCreateThread(id).run(wait, functionToRun);
    }

    wait(id, wait) {
        this.getOrCreateThread(id).wait(wait);
    }

    getOrCreateThread(id) {
        if (this.threadsById[id]) {
            return this.threadsById[id];
        }
        const newThread = new Thread();
        this.threadsById[id] = newThread;
        return newThread
    }

}