# Run Terminal Code References

## Terminal Service (command dispatch)
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/run/terminal/TerminalService.kt`
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/run/terminal/TerminalController.kt`
- **Syntax highlighting**: `backend/src/main/kotlin/org/n1/av2/run/terminal/SyntaxHighlightingService.kt`

## Generic Commands (work outside and inside)
- **Help**: `backend/src/main/kotlin/org/n1/av2/run/terminal/generic/CommandHelpService.kt`
- **Scan**: `backend/src/main/kotlin/org/n1/av2/run/terminal/generic/CommandScanService.kt`
- **Script (run, download)**: `backend/src/main/kotlin/org/n1/av2/run/terminal/generic/CommandScriptService.kt`
- **Social (/share)**: `backend/src/main/kotlin/org/n1/av2/run/terminal/generic/SocialTerminalService.kt`
- **Dev commands**: `backend/src/main/kotlin/org/n1/av2/run/terminal/generic/DevCommandHelper.kt`

## Outside Commands
- **Attack / Quick Attack**: `backend/src/main/kotlin/org/n1/av2/run/terminal/outside/CommandStartAttackService.kt`
- **Outside helper**: `backend/src/main/kotlin/org/n1/av2/run/terminal/outside/OutsideTerminalHelper.kt`

## Inside Commands
- **Move / Quick Move**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandMoveService.kt`
- **Hack / Quick Hack**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandHackService.kt`
- **View**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandViewService.kt`
- **Disconnect**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandDisconnectService.kt`
- **Debug (sweeperunblock)**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandDebugService.kt`
- **Inside helper**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/InsideTerminalHelper.kt`

## Skill-Based Commands
- **Weaken**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandWeakenService.kt`
- **Rollback (undo tripwire)**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandUndoTripwireService.kt`
- **Jump**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandJumpToHackerService.kt`

## Frontend
- **Terminal component**: `frontend/src/common/terminal/Terminal.tsx`
- **Terminal input**: `frontend/src/common/terminal/TerminalInput.tsx`
- **Terminal line rendering**: `frontend/src/common/terminal/TerminalLine.tsx`
