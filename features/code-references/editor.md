# Editor Code References

## Editor Backend
- **Controller**: `backend/src/main/kotlin/org/n1/av2/editor/EditorWsController.kt`
- **Service**: `backend/src/main/kotlin/org/n1/av2/editor/EditorService.kt`
- **Editor commands**: `backend/src/main/kotlin/org/n1/av2/editor/EditorCommands.kt`
- **Editor state entity**: `backend/src/main/kotlin/org/n1/av2/editor/SiteEditorStateEntityAndRepo.kt`
- **Editor state service**: `backend/src/main/kotlin/org/n1/av2/editor/SiteEditorStateEntityService.kt`
- **Validation service**: `backend/src/main/kotlin/org/n1/av2/editor/SiteValidationService.kt`
- **Validation context**: `backend/src/main/kotlin/org/n1/av2/editor/ValidationContext.kt`

## Editor Frontend
- **Editor root**: `frontend/src/editor/EditorRoot.tsx`
- **Editor home**: `frontend/src/editor/component/EditorHome.tsx`
- **Editor main layout**: `frontend/src/editor/component/map/EditorMain.tsx`
- **Editor canvas**: `frontend/src/editor/component/map/canvas/EditCanvasPanel.tsx`
- **Node images**: `frontend/src/editor/component/nodes/EditorNodeImage.tsx`
- **Site properties**: `frontend/src/editor/component/properties/EditorSitePropertiesAndState.tsx`

## Text Editor
- **Text editor root**: `frontend/src/editor-text/EditorTextRoot.tsx`
- **Text editor home**: `frontend/src/editor-text/component/EditorTextHome.tsx`
- **Text editor link**: `frontend/src/editor/component/map/panel/layer/element/TextEditorLink.tsx`

## Layer Validation (Backend)
- **Layer base (validateName)**: `backend/src/main/kotlin/org/n1/av2/layer/Layer.kt`
- **OsLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/os/OsLayer.kt`
- **TextLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/text/TextLayer.kt`
- **KeyStoreLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/keystore/KeyStoreLayer.kt`
- **PasswordIceLayer**: `backend/src/main/kotlin/org/n1/av2/layer/ice/password/PasswordIceLayer.kt`
- **TripwireLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/tripwire/TripwireLayer.kt`
- **TimerAdjusterLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/timeradjuster/TimerAdjusterLayer.kt`
- **ScriptCreditsLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/script/ScriptCreditsLayer.kt`
- **ScriptInteractionLayer**: `backend/src/main/kotlin/org/n1/av2/layer/other/script/ScriptInteractionLayer.kt`

## Layer Editor Panels
- **ICE password panel**: `frontend/src/editor/component/map/panel/layer/type/panel/ice/LayerIcePasswordPanel.tsx`
- **ICE simple panel**: `frontend/src/editor/component/map/panel/layer/type/panel/ice/LayerSimpleIcePanel.tsx`
- **ICE TAR panel**: `frontend/src/editor/component/map/panel/layer/type/panel/ice/LayerIceTarPanel.tsx`
- **Timer adjuster panel**: `frontend/src/editor/component/map/panel/layer/type/panel/app/LayerTimerAdjusterPanel.tsx`
- **Script credits panel**: `frontend/src/editor/component/map/panel/layer/type/panel/app/LayerScriptCreditsPanel.tsx`
- **Script interaction panel**: `frontend/src/editor/component/map/panel/layer/type/panel/app/LayerScriptInteraction.tsx`
