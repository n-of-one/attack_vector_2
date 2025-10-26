import {Page, test, TestInfo} from '@playwright/test';
import * as fs from 'fs';
import pixelmatch from 'pixelmatch';
import {PNG} from 'pngjs';

export async function waitForStableScreenshot(
    page: Page,
    screenshotNumber: number,
    testInfo: TestInfo,
    timeoutMs = 10000,
    intervalMs = 250,
    threshold = 0,
) {

    const path = `${testInfo.file}-snapshots/${testInfo.title}-${screenshotNumber}-chromium-win32.png`

    const referenceBuffer = fs.readFileSync(path)
    const reference = PNG.sync.read(referenceBuffer)

    const startTime = Date.now()

    await test.step(`waiting for visual match with ${path}`, async () => {
        while (Date.now() - startTime < timeoutMs) {
            const currentBuffer = await page.screenshot()
            const current = PNG.sync.read(currentBuffer)

            if (
                current.width === reference.width &&
                current.height === reference.height
            ) {
                const diffPixels = pixelmatch(
                    reference.data,
                    current.data,
                    null,
                    reference.width,
                    reference.height,
                    {threshold: 0.1}
                );

                if (diffPixels <= threshold) {
                    return
                }
            }

            await page.waitForTimeout(intervalMs);
        }

        throw new Error(
            `Timed out waiting for screen to match ${path} after ${timeoutMs}ms`
        )
    })

}