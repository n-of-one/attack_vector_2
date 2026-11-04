# Skills Code References

## Skill System
- **Backend entity & enum**: `backend/src/main/kotlin/org/n1/av2/hacker/skill/SkillEntityAndRepo.kt`
- **Backend service**: `backend/src/main/kotlin/org/n1/av2/hacker/skill/SkillService.kt`
- **Backend controller**: `backend/src/main/kotlin/org/n1/av2/hacker/skill/SkillWsController.kt`

## Skill Usage in Commands
- **Weaken**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandWeakenService.kt`
- **Undo Tripwire (rollback)**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandUndoTripwireService.kt`
- **Jump to Hacker**: `backend/src/main/kotlin/org/n1/av2/run/terminal/inside/skillbased/CommandJumpToHackerService.kt`

## Adding skills
- `SkillService.addNewSkillAndUpdateUi`: GM adds a skill in the UI, always creates a new instance
- `SkillService.addOrUpdateSkillForUser`: login / Frontier sync / dev defaults, reuses the first existing skill of that type

## Trait skill
- **Validation and key parsing**: `backend/src/main/kotlin/org/n1/av2/hacker/skill/SkillEntityAndRepo.kt` (`validateTrait`, `traitKeyOf`)
- **Trait lookup**: `SkillService.userHasTrait`
- **Used by**: `backend/src/main/kotlin/org/n1/av2/layer/other/daemon/DaemonService.kt`

## Frontend
- **Skill display**: `frontend/src/common/users/HackerSkillsElement.tsx` (`TraitSkillValueElement`: key and text inputs)
- **Trait value split/join**: `frontend/src/common/users/HackerSkills.ts` (`splitTraitSkillValue`, `joinTraitSkillValue`)
