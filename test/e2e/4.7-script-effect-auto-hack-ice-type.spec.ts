import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";

const SCRIPT_NAME = "hackicetype"

const SITE_NAME = "dev"


async function startHack(login: LoginPage, hacker: HackerPage) {
    await test.step("Setup", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun(SITE_NAME)
        await hacker.startAttack(START_ATTACK_QUICK)
    })
}

test('4.7.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(25_000);
    const [login, gm, scriptManagement, hacker] = [new LoginPage(page), new GmPage(page), new ScriptManagementPage(page), new HackerPage(page)]

    await test.step("Delete existing scripts", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.deleteAllScriptsFromHacker()
        await scriptManagement.deleteExistingScriptIfItExists(SCRIPT_NAME)
    })

    await test.step("Prepare run", async () => {
        await gm.deleteAllRuns(SITE_NAME)
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
    })
})

test('4.7.1 - Create script and give to hacker', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);
    const [login, scriptManagement] = [new LoginPage(page), new ScriptManagementPage(page)]

    await test.step("Create script", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.createScript(SCRIPT_NAME)
        await scriptManagement.addUsefulEffect("AUTO_HACK_ICE_TYPE", "Automatically hack ICE of a specific type")
    })

    await test.step("Give script to hacker", async () => {
        await scriptManagement.navigateToCurrentScriptsOf("hacker")
        await scriptManagement.addScriptAndLoad(SCRIPT_NAME, 1)
    })
})

test('4.7.2 - Script only hacks matching ICE type', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await startHack(login, hacker)

    await test.step("Fail on non-matching ICE type (Tangle)", async () => {
        await hacker.typeCommand("qmove 01")

        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)

        await hacker.expectTerminalText("This script can only be used on Jaal ICE.")
    })

    await test.step("Succeed on matching ICE type (Word Search)", async () => {
        await hacker.typeCommand("qmove 03")

        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)

        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.expectTerminalText("ICE hack complete.")
        // verify ICE actually hacked.
        await hacker.typeCommand("view", "1 Jaal ICE hack")
    })
})
