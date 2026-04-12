import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {AuthAppPage} from "./testframework/AuthAppPage";
import {wait} from "./testframework/utils/testUtils";

test('test', async ({page}) => {
    test.setTimeout(30_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in as hacker and join existing run dev", async () => {
        await login.loginUsingLink("hacker")
        await hacker.resetRuns("dev")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Open Authentication App for Password ICE in node 06", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 06")
        await hacker.typeCommandDontWaitForPrompt("password 1")
    })

    const appPagePromise = page.waitForEvent('popup');
    const appPage = await appPagePromise;
    const authApp = new AuthAppPage(appPage)

    await test.step("Verify auth app initial state", async () => {
        await authApp.verifyAuthorizationRequired()
        await authApp.verifyPromptText()
        await authApp.waitForPasswordInput()
        await wait(page, 1, "Wait for fade-in of UI.")
        await authApp.verifyHintNotVisible()
        await authApp.verifyIceBanner("Rahasy")
    })

    await test.step("Submit wrong passwords, verify lockout and hint appears", async () => {
        await authApp.submitPassword("wrong1")
        await authApp.verifyLocked(2)
        await authApp.verifyHintNotVisible()
        await authApp.waitForUnlock()

        await authApp.submitPassword("wrong2")
        await authApp.verifyLocked(2)
        await authApp.verifyHintNotVisible()
        await authApp.waitForUnlock()

        await authApp.submitPassword("wrong3")
        await authApp.verifyLocked(2)
        await authApp.waitForUnlock()
        await authApp.verifyHintVisible("it's a kind of fish")
    })

    await test.step("Submit correct password", async () => {
        await authApp.submitPassword("Swordfish")
        await authApp.verifyAuthenticationSuccess()
    })
});
