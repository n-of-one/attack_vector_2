import { test as base, Page, expect } from '@playwright/test';

export const test = base.extend<{
    page: Page
}>({
    page: async ({ page }, use) => {
        await page.addInitScript(() => {
            (globalThis as any).__ENABLE_TEST_SUPPORT__ = true
            // see File: "TestSupport.ts" in the frontend code.
        })

        page.on("console", msg => {
            const text = msg.text()
            if (text.startsWith("BROWSER: ") ) {
                console.log(`[browser] ${text.substring("BROWSER: ".length)}`)
            }
            if (text.startsWith("PLAYWRIGHT: ") ) {
                console.log(`[playwright in browser] ${text.substring("PLAYWRIGHT: ".length)}`)
            }
        })

        await use(page)
    },
})

export { expect };