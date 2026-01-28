import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {GmPage} from "./testframework/GmPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";

const SITE_NAME = "twolayers"

test('1.1.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);

    const [login, gm, hacker] = [new LoginPage(page), new GmPage(page), new HackerPage(page)]

    await login.loginUsingLink("gm")

    await gm.importSiteIfItDoesNotExist(SITE_NAME, "twolayers.json")


    await gm.deleteAllRuns(SITE_NAME)
    await login.loginUsingLink("hacker")
    await hacker.startNewRun(SITE_NAME)
    await hacker.typeCommand("qs")
    await hacker.waitForScanAnimationFinished()
})

test('1.1.1 - Verify node icon before and after hacks', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await login.loginUsingLink("hacker")
    await hacker.joinExistingRun(SITE_NAME)
    await hacker.startAttack(START_ATTACK_QUICK)
    await hacker.verifyScreenshotCanvas("node-unhacked")

    await page.mouse.click(1276,468)
    await hacker.verifyScanInfo("Hacked: No", 2)
    await hacker.verifyScanInfo("Hacked: Yes", 0)
    await page.mouse.click(1000,200)

    await hacker.typeCommand("qhack 2")
    await hacker.verifyScreenshotCanvas("node-unhacked")

    await page.mouse.click(1276,468)
    await hacker.verifyScanInfo("Hacked: No", 1)
    await hacker.verifyScanInfo("Hacked: Yes", 1)
    await page.mouse.click(1100,300)

    await hacker.typeCommand("qhack 1")
    await hacker.waitForNodeHackedAnimationFinished()
    await hacker.verifyScreenshotCanvas("node-hacked")

    await page.mouse.click(1276,468)
    await hacker.verifyScanInfo("Hacked: No", 0)
    await hacker.verifyScanInfo("Hacked: Yes", 2)
    await page.mouse.click(1100,300)
})
