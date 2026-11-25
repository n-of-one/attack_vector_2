# Layers Code References

Layer classes and editor panels are listed in `editor.md`. Hack dispatch per layer type: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandHackService.kt`

## OS / Text Layer
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/other/os/OsLayerService.kt`, `backend/src/main/kotlin/org/n1/av2/layer/other/text/TextLayerService.kt`

## Keystore Layer
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/other/keystore/KeystoreLayerService.kt` (passwords: `KeystoreService.kt`, `IcePasswordEntityAndRepo.kt`)

## Status Light / Lock Layer
- **Backend**: `backend/src/main/kotlin/org/n1/av2/layer/app/status_light/`, `backend/src/main/kotlin/org/n1/av2/layer/app/AppService.kt`

## Daemon Layer
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/other/daemon/DaemonService.kt` (per-hacker state: `DaemonStatusEntityAndRepo.kt`)

## Script Credits / Script Interaction Layer
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/other/script/ScriptCreditsLayerService.kt`, `backend/src/main/kotlin/org/n1/av2/layer/other/script/ScriptInteractionLayerService.kt`

## Network Switch Layer
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/other/networkswitch/NetworkSwitchService.kt` (choice, unlock, connection statuses, reachability)
- **Move check**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/CommandMoveService.kt`
- **Jump / script checks**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandJumpToHackerService.kt`, `backend/src/main/kotlin/org/n1/av2/script/effect/helper/JumpEffectHelper.kt`, `backend/src/main/kotlin/org/n1/av2/script/effect/helper/NodeAccessHelper.kt`
- **Core unlock**: `backend/src/main/kotlin/org/n1/av2/layer/other/core/CoreLayerService.kt`
- **Connection display (map)**: `frontend/src/common/canvas/display/ConnectionDisplay.ts`

## Uplink Layer / Internal Site
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/other/uplink/UplinkService.kt` (hack, arrival, return when leaving)
- **Return hook**: `RunService.hackerDisconnect` (`backend/src/main/kotlin/org/n1/av2/run/RunService.kt`)
- **Entering a site via an uplink (up or down)**: `/run/enterSiteFromUplink` in `RunWsController.kt` → `UplinkService.enterSiteFromUplink`
- **State**: `uplinkReturns` / `uplinkArrivalNodeId` in `backend/src/main/kotlin/org/n1/av2/hacker/hackerstate/HackerStateEntityAndRepo.kt`, `uplinkRunIds` / `parentRunId` in `backend/src/main/kotlin/org/n1/av2/run/entity/RunEntityAndRepo.kt`
- **Hiding internal sites**: `RunService.startNewRunAndReply`, `RunService.prepareToEnterRun`, `backend/src/main/kotlin/org/n1/av2/run/runlink/RunLinkService.kt`
- **Connection sequence and run switch (frontend)**: `frontend/src/hacker/RunServerActionProcessor.ts`
