import {Page} from '@playwright/test'
import {expect, test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {GmPage} from "./testframework/GmPage";
import {SKILL_TRAIT, UsersPage} from "./testframework/UsersPage";
import {AvPage} from "./testframework/AvPage";
import {startQuickHack} from "./testframework/utils/testUtils";

const SITE_NAME = "test-daemon"


const HACK_TEXT = "Hacking this layer reveals nothing new."


test('3.2.0 - Prepare', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, gm, users, hacker] = [new LoginPage(page), new GmPage(page), new UsersPage(page), new HackerPage(page)]

    await test.step("Import site and remove old runs", async () => {
        await login.loginUsingLink("gm")
        await gm.importSiteIfItDoesNotExist(SITE_NAME, "v1-test-daemon.json")
        await gm.deleteAllRuns(SITE_NAME)
    })

    await test.step("Give hacker two traits, and angler none", async () => {
        await users.openUser("hacker")
        await users.addTraitSkillIfMissing("insider", "Former corporate insider")
        await users.addTraitSkillIfMissing("net.ghost", "Ghost in the machine")

        await users.openUser("angler")
        await users.removeAllSkillsOfType(SKILL_TRAIT)
    })

    await test.step("Start run and share with angler", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
        await hacker.typeCommand("/share angler")
    })
})


test('3.2.1 - Daemon triggers for hacker with traits', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Daemon in start node triggers when the attack starts", async () => {
        await startQuickHack(login, hacker, SITE_NAME)
        await hacker.expectTerminalText("daemon-00-start")
    })

    await test.step("First-entry-only triggers on first entry, not on second entry", async () => {
        await hacker.typeCommand("qmove 01", "daemon-01-first-only")
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 01")
        await hacker.expectNotTerminalText("daemon-01-first-only")
    })

    await test.step("Every-entry triggers on each entry", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 02", "daemon-02-every")
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 02", "daemon-02-every")
    })

    await test.step("Each-hacker-once triggers on first entry, not on second entry", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 03", "daemon-03-each-once")
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 03")
        await hacker.expectNotTerminalText("daemon-03-each-once")
    })

    await test.step("Daemons requiring a trait trigger for the hacker that has both traits", async () => {
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 04", "daemon-04-insider")
        await hacker.typeCommand("qmove 00")
        await hacker.typeCommand("qmove 05", "daemon-05-ghost")
    })

    await test.step("Hacking a daemon reveals nothing", async () => {
        await hacker.typeCommand("hack 1", HACK_TEXT)
        await hacker.expectNotTerminalText("daemon-05-ghost")
    })

    await test.step("Hacker only sees the text of the trait skills", async () => {
        await new AvPage(page).navigateToMenuItem("{")
        const app = page.locator("body")
        await expect(app, "Verify trait text is shown").toContainText("Former corporate insider")
        await expect(app, "Verify trait text is shown").toContainText("Ghost in the machine")
        await expect(app, "Verify trait key is hidden").not.toContainText("insider:")
        await expect(app, "Verify trait key is hidden").not.toContainText("ghost:")
        await expect(app, "Verify skill name is hidden").not.toContainText("Trait :")
    })
})


test('3.2.2 - Daemon for second hacker without traits', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, angler] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Join run as angler", async () => {
        await login.loginByTyping("angler")
        await angler.joinExistingRun(SITE_NAME)
        await angler.startAttack(START_ATTACK_QUICK)
        await angler.expectTerminalText("daemon-00-start")
    })

    await test.step("First-entry-only does not trigger, the first hacker already triggered it", async () => {
        await angler.typeCommand("qmove 01")
        await angler.expectNotTerminalText("daemon-01-first-only")
    })

    await test.step("Every-entry triggers", async () => {
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 02", "daemon-02-every")
    })

    await test.step("Each-hacker-once triggers on first entry of this hacker", async () => {
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 03", "daemon-03-each-once")
    })

    await test.step("Daemons requiring a trait do not trigger for hacker without traits", async () => {
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 04")
        await angler.expectNotTerminalText("daemon-04-insider")
        await angler.typeCommand("qmove 00")
        await angler.typeCommand("qmove 05")
        await angler.expectNotTerminalText("daemon-05-ghost")
    })
})


test('3.2.3 - Site reset resets daemons', async ({page}: { page: Page }) => {
    test.setTimeout(30_000);
    const [login, gm, hacker] = [new LoginPage(page), new GmPage(page), new HackerPage(page)]

    await test.step("Reset site", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteAllRuns(SITE_NAME)
    })

    await test.step("First-entry-only triggers again after reset", async () => {
        await login.loginUsingLink("hacker")
        await hacker.startNewRun(SITE_NAME)
        await hacker.typeCommand("qs")
        await hacker.waitForScanAnimationFinished()
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 01", "daemon-01-first-only")
    })
})


test('3.2.4 - Cleanup', async ({page}: { page: Page }) => {
    test.setTimeout(20_000);
    const [login, gm, users] = [new LoginPage(page), new GmPage(page), new UsersPage(page)]

    await test.step("Remove runs and trait skills", async () => {
        await login.loginUsingLink("gm")
        await gm.deleteAllRuns(SITE_NAME)
        await users.openUser("hacker")
        await users.removeAllSkillsOfType(SKILL_TRAIT)
    })
})
