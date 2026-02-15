import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";

const RESET_SCRIPT_NAME = "starttimer"
const SPEEDUP_SCRIPT_NAME = "speedup"


async function startHack(login: LoginPage, hacker: HackerPage) {
    await test.step("Setup", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun("dev")
        await hacker.startAttack(START_ATTACK_QUICK)
    })
}

test('4.1.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, gm, scriptManagement, hacker] = [new LoginPage(page), new GmPage(page), new ScriptManagementPage(page), new HackerPage(page)]

    await test.step("Delete existing scripts", async () => {
        await login.loginUsingLink("gm")

        await scriptManagement.deleteAllScriptsFromHacker()

        await scriptManagement.deleteExistingScriptIfItExists(SPEEDUP_SCRIPT_NAME)
        await scriptManagement.deleteExistingScriptIfItExists(RESET_SCRIPT_NAME)
    })

    await test.step("Prepare run", async () => {
        await gm.deleteAllRuns("dev")
        await login.loginUsingLink("hacker")
        await hacker.startNewRun("dev")
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
    })
})

test('4.1.1 - Create scripts and give them to hacker', async ({page}: { page: Page }) => {
    test.setTimeout(15_000);
    const [login, scriptManagement] = [new LoginPage(page), new ScriptManagementPage(page)]

    await login.loginUsingLink("gm")

    await test.step("Create script: starttimer", async () => {
        await scriptManagement.createScript(RESET_SCRIPT_NAME)
        await scriptManagement.addDrawbackEffect("START_RESET_TIMER", "Start reset timer", "10:00")
        await scriptManagement.verifyHackerEffectDescription(RESET_SCRIPT_NAME, "Start a countdown of 10 minutes to reset the site")
    })

    await test.step("Create script: speedup", async () => {
        await scriptManagement.createScript(SPEEDUP_SCRIPT_NAME)
        await scriptManagement.addDrawbackEffect("SPEED_UP_RESET_TIMER", "Speed up reset timer", "01:00")
        await scriptManagement.verifyHackerEffectDescription(SPEEDUP_SCRIPT_NAME, "If there already was a reset countdown triggered by another script, it is sped up by 1 minute.")
    })


    await test.step("Give script to hacker", async () => {
        await scriptManagement.navigateToCurrentScriptsOf("hacker")
        await scriptManagement.addScriptAndLoad(SPEEDUP_SCRIPT_NAME, 1)
        await scriptManagement.addScriptAndLoad(RESET_SCRIPT_NAME, 1)
    })
})

test('4.1.2 - Script works as intended', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await startHack(login, hacker)

    const scriptCodeSpeedup = await hacker.getCodeForScript(SPEEDUP_SCRIPT_NAME)


    await test.step("Run script starttimer and verify timer started", async () => {
        const scriptCodeStartTimer = await hacker.getCodeForScript(RESET_SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCodeStartTimer} 1`)
        await hacker.expectTerminalText("Script triggered site reset in 10 minutes")
        await hacker.verifyAppText('00:09:5');

    })

    await test.step("Run script speedup and verify timer is sped up", async () => {
        await hacker.typeCommand(`run ${scriptCodeSpeedup}`)

        await hacker.expectTerminalText("Countdown accelerated by 1 minute")
        await hacker.verifyAppText('00:08:5');
    })

    await test.step("Trigger tripwire timer, verify that its a second timer", async () => {
        await hacker.typeCommand("move 08", "Layer 1 triggered site reset in 55 minutes.")

        await hacker.verifyAppText('00:08:');
        await hacker.verifyAppText('00:54:');
    })

    await test.step("Trigger accelerator layer, verify both timers affected", async () => {
        await hacker.typeCommand("move 09",
            ["Shutdown amplifier accelerates existing shutdown timers.", "Countdown accelerated by 1 minute"])

        await hacker.verifyAppText('00:07:');
        await hacker.verifyAppText('00:53:');
    })

})
