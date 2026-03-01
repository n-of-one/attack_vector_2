import {test} from "./testframework/fixtures"
import {LoginPage} from "./testframework/LoginPage";
import {HackerPage, START_ATTACK_QUICK} from "./testframework/HackerPage";
import {PasswordPage} from "./testframework/PasswordPage";
import {wait} from "./testframework/utils/testUtils";

test('test', async ({page}) => {
    test.setTimeout(70_000);

    const [login, hacker] = [new LoginPage(page), new HackerPage(page)]

    await test.step("Log in as hacker and join existing run dev", async () => {
        await login.loginUsingLink("hacker")
        await hacker.resetRuns("dev")
        await hacker.joinExistingRun("dev")
    })

    await test.step("Start Password ICE puzzle in node 06", async () => {
        await hacker.startAttack(START_ATTACK_QUICK)
        await hacker.typeCommand("qmove 06")
        await hacker.typeCommandDontWaitForPrompt("hack 1")
    })

    const icePagePromise = page.waitForEvent('popup');
    const icePage = await icePagePromise;
    const password = new PasswordPage(icePage)

    await test.step("Verify ICE state before hacking", async () => {
        await password.verifyIceName()
        await password.waitForPasswordPrompt()
        await wait(page, 1, "Wait for fade-in of UI.")
        await password.verifyHintNotVisible()
    })

    await test.step("Submit wrong passwords, verify timeout increments and hint appears", async () => {
        await password.submitPassword("wrong1")
        await password.verifyPasswordRejected("wrong1")
        await password.verifyPasswordAttemptListed("wrong1")
        await password.verifyTimeoutBetweenPasswordAttempts(2)
        await password.verifyHintNotVisible()
        await password.waitForUnlock()

        await password.submitPassword("wrong2")
        await password.verifyPasswordAttemptListed("wrong2")
        await password.verifyTimeoutBetweenPasswordAttempts(2)
        await password.verifyHintNotVisible()
        await password.waitForUnlock()

        await password.submitPassword("wrong3")
        await password.verifyPasswordAttemptListed("wrong3")
        await password.verifyTimeoutBetweenPasswordAttempts(2)
        await password.verifyHintVisible("it's a kind of fish")
        await password.waitForUnlock()

        await password.submitPassword("wrong4")
        await password.verifyPasswordAttemptListed("wrong4")
        await password.verifyTimeoutBetweenPasswordAttempts(5)
        await password.waitForUnlock()
    })

    await test.step("Submit duplicate password and verify it is silently ignored", async () => {
        await password.verifyAttemptCount(4)
        await password.submitPassword("wrong1")
        await password.verifyAttemptCount(4)
    })

    await test.step("Submit correct password", async () => {
        await password.submitPassword("Swordfish")
        await password.verifyPasswordAccepted()
    })

    await test.step("Verify node shown as hacked in site map", async () => {
        await hacker.waitForNodeHackedAnimationFinished()
        await hacker.verifyScreenshotCanvas("2.3-password-ice-hacked")
    })
});
