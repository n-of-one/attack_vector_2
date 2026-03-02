import {Page} from '@playwright/test'
import {test} from "./testframework/fixtures"
import {ScriptManagementPage} from "./testframework/ScriptManagementPage";
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";

const SCRIPT_NAME = "rotateice"

const SITE_NAME = "dev"


async function startHack(login: LoginPage, hacker: HackerPage) {
    await test.step("Setup", async () => {
        await login.loginUsingLink("hacker")
        await hacker.joinExistingRun(SITE_NAME)
        await hacker.startAttack(START_ATTACK_QUICK)
    })
}

test('4.6.0 - Prepare', async ({page}: { page: Page }) => {
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

test('4.6.1 - Create script and give to hacker', async ({page}: { page: Page }) => {
    test.setTimeout(10_000);
    const [login, scriptManagement] = [new LoginPage(page), new ScriptManagementPage(page)]

    await test.step("Create script", async () => {
        await login.loginUsingLink("gm")
        await scriptManagement.createScript(SCRIPT_NAME)
        await scriptManagement.addUsefulEffect("ROTATE_ICE", "Rotate ICE - change ICE type")
    })

    await test.step("Give script to hacker", async () => {
        await scriptManagement.navigateToCurrentScriptsOf("hacker")
        await scriptManagement.addScriptAndLoad(SCRIPT_NAME, 4)
    })
})

test('4.6.2 - Script rotates through all ICE types', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await startHack(login, hacker)

    await test.step("Move to node with Word Search ICE", async () => {
        await hacker.typeCommand("qmove 03")
        await hacker.typeCommand("view", "Jaal ICE")
    })

    await test.step("Rotation 1: Word Search -> Tangle", async () => {
        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)
        await hacker.expectTerminalText("ICE type changed to Gaanth ICE.")
        await hacker.typeCommand("view", "Gaanth ICE")
    })

    await test.step("Rotation 2: Tangle -> Netwalk", async () => {
        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)
        await hacker.expectTerminalText("ICE type changed to Sanrachana ICE.")
        await hacker.typeCommand("view", "Sanrachana ICE")
    })

    await test.step("Rotation 3: Netwalk -> Sweeper", async () => {
        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)
        await hacker.expectTerminalText("ICE type changed to Visphotak ICE.")
        await hacker.typeCommand("view", "Visphotak ICE")
    })

    await test.step("Rotation 4: Sweeper -> Word Search (full cycle)", async () => {
        const scriptCode = await hacker.getCodeForScript(SCRIPT_NAME)
        await hacker.typeCommand(`run ${scriptCode} 1`)
        await hacker.expectTerminalText("ICE type changed to Jaal ICE.")
        await hacker.typeCommand("view", "Jaal ICE")
    })
})
