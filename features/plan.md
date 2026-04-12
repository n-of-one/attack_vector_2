# Plan: Feature Representation of Attack Vector

## Context

Attack Vector 2 is a LARP hacking simulation. We need a clear, reviewable catalog of **what the game does** — every feature with its capabilities, organized by area. This will be reused for multiple code activities beyond just test generation. Code references are kept in separate files.

## What We Produced

A top-level `features/` directory:

```
features/
  README.md
  plan.md                          ← This file
  features/
    authentication.md
    hacker-home.md
    run-terminal.md
    run-ui.md
    ice/
      password.md
      netwalk.md
      sweeper.md
      tangle.md
      tar.md
      wordsearch.md
    scripts/
      management.md
      effects.md
      marketplace.md
    timers.md
    skills.md
    gm.md
    editor.md
    admin.md
    multi-user.md
  code-references/
    authentication.md
    hacker-home.md
    run-terminal.md
    run-ui.md
    ice.md
    scripts.md
    timers.md
    skills.md
    gm.md
    editor.md
    admin.md
    multi-user.md
```

## Feature File Format

Each file has a short intro describing the feature, then a `## Capabilities` section with concise bullet points:

```markdown
# Password ICE

Password ICE protects nodes with a text password. The hacker must
know the password in order to 'hack' it.

## Capabilities
- two UIs: password ICE and Authentication app
- correct password hacks ICE, incorrect password is rejected
- incremental timeouts between wrong entries
- hint after 3rd wrong entry
- accepts both static password and keystore password
- password ICE UI shows wrong entries, authentication app does not
- can switch from authentication app to password ICE UI
```

Each capability is a brief statement of what the feature does — not a step-by-step test procedure. Files with multiple features (like `run-terminal.md`) use `#` headings per feature, each with its own `## Capabilities` list.

## Code Reference File Format

```markdown
# ICE Code References

## Password ICE
- **Backend controller**: `backend/.../PasswordIceWsController.kt`
- **Backend service**: `backend/.../PasswordIceService.kt`
- **Frontend component**: `frontend/src/standalone/ice/password/PasswordHome.tsx`
```

## How It Was Built

### Step 1: Deep-read implementation code

For each feature area, read the actual implementation (not just enums/controllers) to capture the real capabilities — including UI details, error handling, edge cases. Key source-of-truth files:

- `backend/.../TerminalService.kt` — terminal command dispatch
- `backend/.../enums/LayerType.kt` — 16 layer types
- `backend/.../ScriptEffectType.kt` — 19 script effects
- `backend/.../SkillEntityAndRepo.kt` — 11 skill types
- Each ICE type's service + controller + frontend component
- Each script effect's handler
- Frontend route tree + page components

### Step 2: Write feature files

For each area, wrote markdown files with concise capability bullets.

### Step 3: Write code reference files

For each area, wrote companion files linking features to backend/frontend source paths.

### Step 4: Write README.md

Explains directory structure and purpose.

## Verification

Verified completeness against backend enums:
- **20/20** script effect types documented ✓
- **11/11** skill types documented ✓
- **16/16** layer types documented ✓ (6 ICE in ice/, 10 non-ICE in editor.md)
- **20/20** terminal commands documented ✓
- **21/21** frontend pages covered ✓

## Next Steps

1. User reviews feature files for completeness and accuracy
2. Map existing E2E test coverage to features
3. Identify gaps and prioritize new tests
4. Generate new E2E tests in existing Playwright style
