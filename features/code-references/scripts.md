# Scripts Code References

## Script Type Management
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/script/type/ScriptTypeWsController.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/script/type/ScriptTypeService.kt`
- **Frontend management**: `frontend/src/gm/scripts/scriptType/ScriptTypeManagement.tsx`
- **Frontend strength config**: `frontend/src/gm/scripts/scriptType/ScriptTypeManagementHackIceByStrength.tsx`
- **Frontend effect config**: `frontend/src/gm/scripts/scriptType/ScriptEffectConfiguration.tsx`

## Script Effects
- **Effect type enum**: `backend/src/main/kotlin/org/n1/av2/script/effect/ScriptEffectType.kt`
- **Effect interface**: `backend/src/main/kotlin/org/n1/av2/script/effect/ScriptEffectInterface.kt`
- **Effect helper**: `backend/src/main/kotlin/org/n1/av2/script/effect/helper/ScriptEffectHelper.kt`
- **UI description utility**: `backend/src/main/kotlin/org/n1/av2/script/common/ScriptEffectUiUtil.kt`

### Positive Effects
- **Auto hack any ICE**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/AutoHackAnyIceEffectService.kt`
- **Auto hack ICE type**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/AutoHackIceTypeEffectService.kt`
- **Auto hack by strength**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/AutoHackIceByStrengthEffectService.kt`
- **Auto hack specific layer**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/AutoHackSpecificIceLayerEffectService.kt`
- **Hack below non-hacked ICE**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/HackBelowNonHackedIceEffectService.kt`
- **Rotate ICE**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/RotateIceEffectService.kt`
- **Sweeper unblock**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/SweeperUnblockEffectService.kt`
- **Tangle reveal clusters**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/TangleRevealClustersEffectService.kt`
- **Word search next words**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ice/WordSearchNextWordsEffectService.kt`
- **Jump to node**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/JumpToNodeEffectService.kt`
- **Jump to hacker**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/JumpToHackerEffectService.kt`
- **Scan beyond ICE node**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ScanBeyondIceNodeEffectService.kt`
- **Delay tripwire**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/DelayTripwireCountdownEffectService.kt`
- **Show message**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/ShowMessageEffectService.kt`
- **Site stats**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/SiteStatsEffectService.kt`
- **Script layer interaction**: `backend/src/main/kotlin/org/n1/av2/script/effect/positive/InteractWithScriptLayerEffectService.kt`

### Negative Effects
- **Start reset timer**: `backend/src/main/kotlin/org/n1/av2/script/effect/negative/StartResetTimerEffectService.kt`
- **Speed up reset timer**: `backend/src/main/kotlin/org/n1/av2/script/effect/negative/SpeedUpResetTimerEffectService.kt`
- **Decrease future timers**: `backend/src/main/kotlin/org/n1/av2/script/effect/negative/DecreaseFutureTimersEffectService.kt`
- **Hidden effects**: `backend/src/main/kotlin/org/n1/av2/script/effect/negative/HiddenEffectsService.kt`

## Script Service & Marketplace
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/script/ScriptWsController.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/script/ScriptService.kt`
- **Script access controller**: `backend/src/main/kotlin/org/n1/av2/script/access/ScriptAccessWsController.kt`
- **Script access service**: `backend/src/main/kotlin/org/n1/av2/script/access/ScriptAccessService.kt`
- **Script income controller**: `backend/src/main/kotlin/org/n1/av2/script/income/ScriptIncomeWsController.kt`
- **Script income service**: `backend/src/main/kotlin/org/n1/av2/script/income/ScriptIncomeService.kt`
- **Script income entity service**: `backend/src/main/kotlin/org/n1/av2/script/income/ScriptIncomeEntityService.kt`

## Script-Related Layers
- **Script credits layer**: `backend/src/main/kotlin/org/n1/av2/layer/other/script/ScriptCreditsLayerService.kt`
- **Script interaction layer**: `backend/src/main/kotlin/org/n1/av2/layer/other/script/ScriptInteractionLayerService.kt`

## Frontend
- **GM script home**: `frontend/src/gm/scripts/GmScriptHome.tsx`
- **GM script access**: `frontend/src/gm/scripts/access/ScriptAccessManagement.tsx`
- **GM current scripts**: `frontend/src/gm/scripts/currentScripts/CurrentScriptManagement.tsx`
- **GM script income**: `frontend/src/gm/scripts/income/ScriptIncome.tsx`
- **Hacker marketplace**: `frontend/src/hacker/market/HackerScriptMarket.tsx`
- **Hacker scripts home**: `frontend/src/hacker/scripts/HackerScriptsHome.tsx`
- **Hacker scripts panel (in-run)**: `frontend/src/hacker/scripts/HackerScriptsPanel.tsx`
- **Hacker credits**: `frontend/src/hacker/credits/HackerScriptCredits.tsx`
- **Script line component**: `frontend/src/common/script/ScriptLine.tsx`
- **Script effects display**: `frontend/src/common/script/type/ScriptEffects.tsx`
