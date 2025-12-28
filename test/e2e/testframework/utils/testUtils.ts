import {expect, Page} from "@playwright/test";

export function confirmDialog(page: Page, expected: string) {
    page.once('dialog', dialog => {
        log(`Dialog message received: ${dialog.message()}`)
        expect(dialog.message(), `Verify dialog message: ${expected}`).toContain(expected)
        dialog.accept().catch(() => {
        })
    })
}

export function appLocator(page: Page) {
    return page.locator('#app')
}

export function log(message: string) {
    console.log(`[playwright] ${message}`)
}

export async function  wait(page: Page, waitSeconds: number, reason: string) {
    log(`waiting for: ${waitSeconds} seconds. Reason: ${reason}`)
    await page.waitForTimeout(waitSeconds * 1000)
}

export function countOccurrences(haystack: string, needle: string):number {
    let count = 0;
    for (let pos = 0; ; ) {
        const idx = haystack.indexOf(needle, pos)
        if (idx === -1) return count
        count++
        pos = idx + needle.length
    }
}