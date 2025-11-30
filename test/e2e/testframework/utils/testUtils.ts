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
    console.log(message)
}

export async function  wait(page: Page, waitSeconds: number, reason: string) {
    log(`waiting for: ${waitSeconds} seconds. Reason: ${reason}`)
    await page.waitForTimeout(waitSeconds * 1000)
}

