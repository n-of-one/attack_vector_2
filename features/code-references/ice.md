# ICE Code References

## Common ICE Infrastructure
- **ICE service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/common/IceService.kt`
- **ICE authorization**: `backend/src/main/kotlin/org/n1/av2/layer/ice/common/IceAuthorizationService.kt`
- **ICE base layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/common/IceLayer.kt`
- **Generic ICE controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/common/GenericIceController.kt`
- **ICE controller (enter/leave)**: `backend/src/main/kotlin/org/n1/av2/layer/ice/IceController.kt`
- **Hacked utility**: `backend/src/main/kotlin/org/n1/av2/layer/ice/HackedUtil.kt`
- **Frontend ICE routing (external)**: `frontend/src/standalone/ice/ExternalHackIce.tsx`
- **Frontend ICE routing (in-site)**: `frontend/src/standalone/ice/SiteHackIce.tsx`
- **Hacker presence on ICE**: `frontend/src/standalone/ice/common/IceHackerPresence.tsx`
- **ICE title bar**: `frontend/src/standalone/ice/common/IceTitle.tsx`

## Password ICE
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/password/PasswordIceController.kt`
- **Backend layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/password/PasswordIceLayer.kt`
- **Auth app service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/password/AuthAppService.kt`
- **Status entity**: `backend/src/main/kotlin/org/n1/av2/layer/ice/password/IcePasswordStatusEntityAndRepo.kt`
- **Frontend root**: `frontend/src/standalone/ice/password/PasswordRoot.tsx`
- **Frontend container**: `frontend/src/standalone/ice/password/container/PasswordContainer.tsx`
- **Frontend home**: `frontend/src/standalone/ice/password/container/PasswordIceHome.tsx`
- **Editor panel**: `frontend/src/editor/component/map/panel/layer/type/panel/ice/LayerIcePasswordPanel.tsx`

## Netwalk ICE
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/netwalk/NetwalkIceController.kt`
- **Backend layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/netwalk/NetwalkIceLayer.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/netwalk/NetwalkIceService.kt`
- **Backend entity**: `backend/src/main/kotlin/org/n1/av2/layer/ice/netwalk/NetwalkIceEntityAndRepo.kt`
- **Puzzle creator**: `backend/src/main/kotlin/org/n1/av2/layer/ice/netwalk/NetwalkCreator.kt`
- **Connection tracer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/netwalk/NetwalkConnectionTracer.kt`
- **Frontend root**: `frontend/src/standalone/ice/netwalk/NetwalkRoot.tsx`
- **Frontend container**: `frontend/src/standalone/ice/netwalk/component/NetwalkContainer.tsx`
- **Frontend home**: `frontend/src/standalone/ice/netwalk/component/NetwalkHome.tsx`

## Sweeper ICE
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/sweeper/SweeperIceController.kt`
- **Backend layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/sweeper/SweeperIceLayer.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/sweeper/SweeperService.kt`
- **Frontend root**: `frontend/src/standalone/ice/sweeper/SweeperRoot.tsx`
- **Frontend container**: `frontend/src/standalone/ice/sweeper/component/SweeperContainer.tsx`
- **Frontend home**: `frontend/src/standalone/ice/sweeper/component/SweeperHome.tsx`

## Tangle ICE
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tangle/TangleIceController.kt`
- **Backend layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tangle/TangleIceLayer.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tangle/TangleService.kt`
- **Backend entity**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tangle/TangleIceEntityAndRepo.kt`
- **Frontend root**: `frontend/src/standalone/ice/tangle/TangleRoot.tsx`
- **Frontend container**: `frontend/src/standalone/ice/tangle/component/TangleContainer.tsx`
- **Frontend home**: `frontend/src/standalone/ice/tangle/component/TangleIceHome.tsx`

## TAR ICE
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tar/TarIceController.kt`
- **Backend layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tar/TarIceLayer.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tar/TarService.kt`
- **Backend entity**: `backend/src/main/kotlin/org/n1/av2/layer/ice/tar/TarIceEntityAndRepo.kt`
- **Frontend root**: `frontend/src/standalone/ice/tar/TarRoot.tsx`
- **Frontend container**: `frontend/src/standalone/ice/tar/component/TarContainer.tsx`
- **Frontend home**: `frontend/src/standalone/ice/tar/component/TarHome.tsx`
- **Editor panel**: `frontend/src/editor/component/map/panel/layer/type/panel/ice/LayerIceTarPanel.tsx`

## Word Search ICE
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/layer/ice/wordsearch/WordSearchIceController.kt`
- **Backend layer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/wordsearch/WordSearchIceLayer.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/layer/ice/wordsearch/WordSearchService.kt`
- **Frontend root**: `frontend/src/standalone/ice/wordsearch/WordSearchRoot.tsx`
- **Frontend container**: `frontend/src/standalone/ice/wordsearch/component/WordSearchContainer.tsx`
- **Frontend home**: `frontend/src/standalone/ice/wordsearch/component/WordSearchHome.tsx`
- **Puzzle component**: `frontend/src/standalone/ice/wordsearch/component/WordSearchPuzzle.tsx`
- **Letter grid**: `frontend/src/standalone/ice/wordsearch/component/LetterGrid.tsx`
