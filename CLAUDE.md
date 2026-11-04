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

## Commands

- Backend tests need no database (integration tests mock Mongo): `MONGODB_URI='mongodb://localhost:1/admin' mvn test` from `backend/`. Single test: add `-Dtest=ClassName`.
- E2E: `npx playwright test` from `test/`. Must run with `workers: 1` (a user can only be logged in once) and a 1920x1080 viewport (the app auto-scales).

## Code style

- 4-space indent, max line length 160 (see `.editorconfig`). Frontend: no semicolons, single quotes.
- Kotlin: use imports, not fully-qualified class references.

## Architecture

- The server owns all game state; the client has no intermediate state. See `frontend/doc/software-design/client-server-interaction.txt` and `backend/doc/WebsocketConnections.md`.
- `features/` is a feature catalog: `features/features/*.md` describes features, `features/code-references/*.md` maps them to Kotlin/TS files. Check it first when locating code for a feature.
- Follow `frontend/doc/coding-recipies.md` and `backend/doc/coding-recipies.txt` for recurring tasks (e.g. adding a layer type).
- Env config lives in `backend/src/main/kotlin/org/n1/av2/platform/config/StaticConfig.kt` (`MONGODB_URI`, `MONGODB_NAME`, `TIME_ZONE`, `development`).

## Release notes

New entries go at the top of `RELEASE_NOTES.md`, formatted `# Release notes AV X.Y.Z (27 October 2026)` with `## Features` / `## Bugfixes` sections.
