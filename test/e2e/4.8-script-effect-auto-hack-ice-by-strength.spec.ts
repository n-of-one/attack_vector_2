import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";

const SCRIPT_NAME = "hack-ice-by-str"

const SITE_NAME = "dev"


async function startHack(login: LoginPage, hacker: HackerPage) {
    await test.step("Setup", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun(SITE_NAME)
        await hacker.startAttack(START_ATTACK_QUICK)
    })
}

test('4.8.0 - Prepare', async ({page}: { page: Page }) => {
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

test('4.8.1 - Create script and give to hacker', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);
    const [login, scriptManagement] = [new LoginPage(page), new ScriptManagementPage(page)]

    await test.step("Create script", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.createScript(SCRIPT_NAME)
        await scriptManagement.addUsefulEffect("AUTO_HACK_ICE_BY_STRENGTH", "Automatically hack ICE by strength")
        await scriptManagement.setHackIceByStrengthLevel("AVERAGE")
    })

    await test.step("Give script to hacker", async () => {
        await scriptManagement.navigateToCurrentScriptsOf("hacker")
        await scriptManagement.addScriptAndLoad(SCRIPT_NAME, 1)
    })
})

test('4.8.2 - Script effect respects strength and exclusions', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await startHack(login, hacker)

    const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)

    await test.step("Fail on ICE above strength - VERY_STRONG netwalk (node 11)", async () => {
        await hacker.typeCommand("qmove 11")
        await hacker.typeCommand(`run ${scriptCode} 1`)
        await hacker.expectTerminalText("Script is too weak to automatically hack ICE of strength: Very strong.")
    })

    await test.step("Fail on excluded ICE type - AVERAGE tar (node 13)", async () => {
        await hacker.typeCommand("qmove 13")
        await hacker.typeCommand(`run ${scriptCode} 1`)
        await hacker.expectTerminalText("Script cannot hack this type of ICE.")
    })

    await test.step("Succeed on ICE at or below strength - WEAK tangle (node 10)", async () => {
        await hacker.typeCommand("qmove 10")
        await hacker.typeCommand(`run ${scriptCode} 2`)
        await hacker.expectTerminalText("ICE hack complete.")
        await hacker.typeCommand("view", "2 Gaanth ICE hacked")
    })
})
