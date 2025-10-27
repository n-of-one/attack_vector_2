import { test, expect } from '@playwright/test';
import {AvPage} from "./testframework/AvPage";

test('test', async ({ page }) => {
    test.setTimeout(70_000);

    const av = new AvPage(page)

    await av.loginOrCreateUserAndLogin("tangle")
    // await av.startNewRun("e2e-tangle")
    // await av.typeCommand("qa")
    // await av.waitForPrompt()
    //
    // await av.typeCommand("hack 1")
    //
    // const icePagePromise = page.waitForEvent('popup');
    // const icePage = await icePagePromise;
    // await expect(icePage.getByText("Ice:")).toContainText("Gaanth");
    // const ice = new AvPage(icePage)

    await page.goto("http://localhost:3000/x/aWJnLHdscmJAaGlgI3lvYXd9dz51dnNzNS18eis=")

    const ice = new AvPage(page)
    const icePage = page

    await ice.wait(5.0)

    await icePage.mouse.move(1135,129)
    await ice.wait(0.1)
    await icePage.mouse.down()
    await ice.wait(0.1)
    await icePage.mouse.move(1135,329, { steps: 20 })
    await ice.wait(0.1)
    await icePage.mouse.up()
    await ice.wait(0.1)
    await ice.wait(1.0)


});