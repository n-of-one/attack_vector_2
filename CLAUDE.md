# CLAUDE.md

## Stack

- `backend/`: Kotlin 2.4, Spring Boot 4, Java 21, Maven. MongoDB. Tests: JUnit + MockK.
- `frontend/`: React 18, Redux + RTK, TypeScript, create-react-app, npm. STOMP over websocket (`webstomp-client`), fabric.js canvases.
- `test/`: Playwright e2e tests. 
- `website/`: Docusaurus.

## Rules

- Never commit or push unless explicitly asked.
- Never run `frontend/build.sh` or modify `backend/src/main/resources/static` unless explicitly asked (built assets are committed there at release time).
- Do not start the backend or frontend; the user runs them manually. E2E tests assume both are already running.
- Only run e2e tests when the user asks; when they do, assume backend and frontend are running.
- Scratch files go in `tmp/` at the project root (gitignored).

## Commands

Run all commands from the project root. Never `cd` (not even `cd backend && ...`): it triggers an approval prompt.

- Backend tests (no database needed, integration tests mock Mongo):
  `mvn -q -f backend/pom.xml test -DMONGODB_URI=mongodb://localhost:1/admin`. Single test: add `-Dtest=ClassName`.
- Frontend type check: `npx --prefix frontend tsc --noEmit -p frontend`
- E2E: `test/node_modules/.bin/playwright test --config test/playwright.config.ts --reporter=line` (optionally followed by a spec, e.g. `e2e/3.2-daemon.spec.ts`).
  Use `--reporter=line`: the configured `html` reporter is not readable from the terminal. A full run takes about 20 minutes; run it in the background.
  Config uses `workers: 1` (a user can only be logged in once) and a 1920x1080 viewport (the app auto-scales).

## Code style

- 4-space indent, max line length 160 (see `.editorconfig`). Frontend: no semicolons, single quotes.
- Kotlin: use imports, not fully-qualified class references.
- Comments: match the sparse style of the surrounding code. Use a short trailing comment only where the code
  can't explain itself (a non-obvious edge case, or a line that looks safe to "simplify" but isn't).
  Doc blocks only rarely. No comments that describe the feature being built, restate the function name,
  or narrate the change; feature explanations belong in `features/` and `website/docs/`.
  In tests, a short description of the setup (e.g. a diagram of the test board) is fine.

## Architecture

- The server owns all game state; the client has no intermediate state. See `frontend/doc/software-design/client-server-interaction.txt` and `backend/doc/WebsocketConnections.md`.
- `features/` is a feature catalog: `features/features/*.md` describes features, `features/code-references/*.md` maps them to Kotlin/TS files. Check it first when locating code for a feature.
- Follow `frontend/doc/coding-recipies.md` and `backend/doc/coding-recipies.txt` for recurring tasks (e.g. adding a layer type).
- Env config lives in `backend/src/main/kotlin/org/n1/av2/platform/config/StaticConfig.kt` (`MONGODB_URI`, `MONGODB_NAME`, `TIME_ZONE`, `development`).

## Release notes

New entries go at the top of `RELEASE_NOTES.md`, formatted `# Release notes AV X.Y.Z (27 October 2026)` with `## Features` / `## Bugfixes` sections.
