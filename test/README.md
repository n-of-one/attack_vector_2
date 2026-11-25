# Test setup

Testing is done with PlayWright.

AttackVector relies heavily on interaction between the browser and the server. 
Playwright allows testing that the interaction work correctly, rather than
that individual components work correctly.


## Manual setup
- You need to manually start AttackVector (frontend and backend).
- The database needs to be compatible. The easiest way to make a compatible database is to start from a clean database.


## Running tests
Run all tests: `npx playwright test` 

Run tests in the UI: `npx playwright test --ui`

Run a single test to check that the server is running with compatible configuration for the tests:

`npx playwright test --project=chromium e2e/1.0-happy-flow.spec.ts`



## Developing new tests

Use: `npx playwright codegen localhost:3000 --viewport-size="1920,1080"`

Setting the viewport is important, otherwise AttackVector will auto-scale the view.
This will then mess up any clicks done with the mouse, as the coordinates will be different.

When adding a browser, set the correct viewport in the playwright.config.ts file.



