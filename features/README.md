# Attack Vector 2 — Feature Catalog

A structured catalog of every feature in Attack Vector 2, organized by area. This catalog is designed to be used as an intermediate representation for generating tests, planning refactors, and understanding the application's capabilities.

## Directory Structure

```
features/
  features/             ← What the game does (pure functionality)
    authentication.md
    hacker-home.md
    run-terminal.md     ← All terminal commands
    run-ui.md           ← Canvas, scan panel, script panel, timers
    ice/                ← One file per ICE type
      password.md
      netwalk.md
      sweeper.md
      tangle.md
      tar.md
      wordsearch.md
    scripts/
      management.md     ← Script type creation, distribution, RAM
      effects.md        ← All 19 script effect types
      marketplace.md    ← Buying, selling, credits, income
    timers.md
    skills.md           ← All 11 hacker skill types
    gm.md
    editor.md
    admin.md
    multi-user.md
  code-references/      ← Where each feature lives in the codebase
    (mirrors feature areas, one file per area)
```

## How to Read Feature Files

Each feature file has:
- **`#` heading** — A feature (e.g., "Password ICE", "scan" command)
- **Intro paragraph** — Brief description of what the feature is
- **`## Capabilities`** — Bullet list of what the feature can do

Features describe _what the game does_, not how it's implemented or how to test it.

## How to Read Code Reference Files

Each code reference file maps features to their backend (Kotlin) and frontend (TypeScript/React) source files. Use these to locate the implementation when you need to understand or modify a feature.

## Feature Areas

| Area | File(s) | Description |
|------|---------|-------------|
| Authentication | `authentication.md` | Login flows, logout, role routing |
| Hacker Home | `hacker-home.md` | Run listings, start/join runs, preferences |
| Run Terminal | `run-terminal.md` | All terminal commands during a hacking run |
| Run UI | `run-ui.md` | Canvas, scan info panel, script panel, timers |
| ICE | `ice/*.md` | 6 puzzle types that protect network nodes |
| Scripts | `scripts/*.md` | Script types, 19 effects, marketplace |
| Timers | `timers.md` | Tripwire countdowns, timer adjusters, shutdown |
| Skills | `skills.md` | 11 hacker skill types |
| GM | `gm.md` | Site management, user management, statistics |
| Editor | `editor.md` | Visual site topology editor |
| Admin | `admin.md` | System configuration, monitoring |
| Multi-User | `multi-user.md` | Collaborative hacking, presence, WebSocket sync |
