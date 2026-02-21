import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {startQuickHack} from "./testframework/utils/testUtils";

const SITE_NAME = "test-timer-adjuster"


test('3.1.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(15_000);
    const [login, gm, hacker] = [new LoginPage(page), new GmPage(page), new HackerPage(page)]

    await test.step("Prepare run", async () => {
        test.setTimeout(15_000);
        await login.loginUsingLink("gm")
        await gm.importSiteIfItDoesNotExist(SITE_NAME, "v1-test-timer-adjuster.json")

        await gm.deleteAllRuns(SITE_NAME)
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
        await hacker.typeCommand("/share angler")
    })
})


test('3.1.1 - Verify adjuster effect first hacker ', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Attack and check timer running", async () => {
        await startQuickHack(login, hacker, SITE_NAME)
        await hacker.expectTerminalText("Layer 1 triggered site reset in 50 minutes.")
        await hacker.verifyAppText('00:49:') // timer is at 00:49:xx
    })


    await test.step("Verify first-entry-only triggers on first entry", async () => {
        await hacker.typeCommand("qmove 01", ["Timer accelerator speeds up existing shutdown timers.", "Countdown accelerated by 1 minute"])
        await hacker.verifyAppText('00:48:') // timer is at 00:48:xx
    })


    await test.step("Verify first-entry-only doesn't trigger on second entry", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 01")
        await hacker.waitForPrompt()
        await hacker.expectNotTerminalText("Countdown accelerated by 1 minute")
        await hacker.verifyAppText('00:48:') // timer is still at 00:48:xx, timer did not trigger again

    })

    await test.step("Verify every-entry triggers on first entry", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 02", ["Timer accelerator speeds up existing shutdown timers.", "Countdown accelerated by 1 minute"])
        await hacker.verifyAppText('00:47:') // timer is at 00:47:xx , reduced by 1 minute
    })

    await test.step("Verify every-entry triggers on second entry", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 02", ["Timer accelerator speeds up existing shutdown timers.", "Countdown accelerated by 1 minute"])
        await hacker.verifyAppText('00:46:') // timer is at 00:46:xx , reduced by 1 minute
    })


    await test.step("Verify each-hacker-once triggers on first entry", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 03", ["Timer accelerator speeds up existing shutdown timers.", "Countdown accelerated by 1 minute"])
        await hacker.verifyAppText('00:45:') // timer is at 00:45:xx , reduced by 1 minute
    })

    await test.step("Verify each-hacker-once doesn't trigger on second second", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 03")
        await hacker.waitForPrompt()
        await hacker.expectNotTerminalText("Countdown accelerated by 1 minute")
        await hacker.verifyAppText('00:45:') // timer is still at 00:45:xx, timer did not trigger again
    })

})

test('3.1.2 - Verify adjuster effect second hacker', async ({page}: { page: Page }) => {
    test.setTimeout(15_000);

    const [login, angler] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Join angler via shared run link", async () => {
        await login.loginByTyping("angler")
        await angler.joinExistingRun(SITE_NAME)
        await angler.startAttack(true)
        await angler.verifyAppText('00:45:') // timer is at 00:45:xx
    })


    await test.step("Verify first-entry-only doesn't trigger on entry by Angler", async () => {
        await angler.typeCommand("qmove 01")
        await angler.waitForPrompt()
        await angler.expectNotTerminalText("Countdown accelerated by 1 minute")
        await angler.verifyAppText('00:45:') // timer is still at 00:45:xx, timer did not trigger again

    })

    await test.step("Verify every-entry triggers on entry by Angler", async () => {
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 02", ["Timer accelerator speeds up existing shutdown timers.", "Countdown accelerated by 1 minute"])
        await angler.verifyAppText('00:44:') // timer is at 00:44:xx , reduced by 1 minute
    })

    await test.step("Verify each-hacker-once triggers on first entry of Angler", async () => {
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 03", ["Timer accelerator speeds up existing shutdown timers.", "Countdown accelerated by 1 minute"])
        await angler.verifyAppText('00:43:') // timer is at 00:43:xx , reduced by 1 minute
    })

    await test.step("Verify each-hacker-once doesn't trigger on second second entry of Angler", async () => {
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 03")
        await angler.waitForPrompt()
        await angler.expectNotTerminalText("Countdown accelerated by 1 minute")
        await angler.verifyAppText('00:43:') // timer is still at 00:43:xx, timer did not trigger again
    })

    await test.step("Verify decelerate increases timer", async () => {
        await angler.typeCommand("qmove 04", ["Timer decelerator slows down existing shutdown timers.", "Tripwire countdown delayed by 5 minutes"])
        await angler.verifyAppText('00:48:') // timer is still back to 00:48:xx, timer increased by 5 minutes
    })


})

test('3.1.3 - cleanup', async ({page}: { page: Page }) => {
    await test.step("Reset site to remove timer", async () => {
        const [login, gm] = [new LoginPage(page), new GmPage(page)]
        await login.loginUsingLink("gm")
        await gm.deleteAllRuns(SITE_NAME)
    })
})
