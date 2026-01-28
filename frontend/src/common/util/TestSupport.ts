import {Schedule} from "./Schedule";


export class TestSupport {

    enabled: boolean = false
    eventFired: string | null = null

    constructor() {
        this.enabled = (globalThis as any).__ENABLE_TEST_SUPPORT__ === true
    }

    log(message: string) {
        if (!this.enabled) {
            return
        }
        console.log(`BROWSER: ${message}`)
    }

    logFromPlaywright(message: string) {
        if (!this.enabled) {
            return
        }
        console.log(`PLAYWRIGHT: ${message}`)
    }

    scheduleEmitEvent(eventName: string, schedule: Schedule, ticks: number) {
        if (!this.enabled) {
            return
        }
        if (!schedule.active) {
            this.log(`Cannot schedule event ${eventName} because the scheduler is not active`)
            throw new Error(`Cannot schedule event ${eventName} because the scheduler is not active`)
        }

        this.log(`Scheduling event: ${eventName} in ${ticks} ticks. `)
        schedule.wait(ticks + 1)
        schedule.run(0, () => {
            this.log(`Firing event: ${eventName}`)
            window.dispatchEvent(new CustomEvent(eventName));
            this.eventFired = eventName
        })

        this.eventFired = null
    }

    // Called by Playwright.
    waitForEvent(eventName: string): Promise<void> {
        if (this.eventFired === eventName) {
            this.eventFired = null
            this.logFromPlaywright(`eventFired before we could wait for it: ${eventName} `)
            return Promise.resolve()
        }

        return new Promise<void>(resolve => {
            const handler = (_event: Event) => {
                this.logFromPlaywright(`Received event: ${eventName} `)
                this.eventFired = null;
                resolve();
            };

            window.addEventListener(eventName, handler, { once: true });
        });
    }
}

export const testSupport = new TestSupport();

(globalThis as any).__testSupport = testSupport
