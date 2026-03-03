import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";

const SCRIPT_NAME = "sitestats"

const SITE_NAME = "dev"


async function startHack(login: LoginPage, hacker: HackerPage) {
    await test.step("Setup", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun(SITE_NAME)
        await hacker.startAttack(START_ATTACK_QUICK)
    })
}

test('4.4.0 - Prepare', async ({page}: { page: Page }) => {
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

test('4.4.1 - Create script and give to hacker', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);
    const [login, scriptManagement] = [new LoginPage(page), new ScriptManagementPage(page)]

    await test.step("Create script", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.createScript(SCRIPT_NAME)
        await scriptManagement.addUsefulEffect("SITE_STATS", "Site stats")
    })

    await test.step("Give script to hacker", async () => {
        await scriptManagement.navigateToCurrentScriptsOf("hacker")
        await scriptManagement.addScriptAndLoad(SCRIPT_NAME, 1)
    })
})

test('4.4.2 - Script displays site stats', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await startHack(login, hacker)

    await test.step("Run script and verify site stats are shown", async () => {
        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode}`)

        await hacker.expectTerminalText([
            "Site stats",
            "Nodes: 15",
            "Core : 1 ",
            "Tripwire : 2 ",
            "Tar ICE (tar) : 2 (strongest: Average) ",
            "Gaanth ICE (tangle) : 3 (strongest: Average) ",
            "Rahasy ICE (password) : 1 (strongest: Very weak) ",
            "Jaal ICE (word search) : 2 (strongest: Onyx) ",
            "Sanrachana ICE (netwalk) : 2 (strongest: Very strong) ",
            "Visphotak ICE (minesweeper): 2 (strongest: Average) "
        ])
    })
})
