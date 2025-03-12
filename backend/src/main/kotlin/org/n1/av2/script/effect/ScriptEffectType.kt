package org.n1.av2.script.effect

import org.n1.av2.script.effect.negative.DecreaseFutureTimersEffectService
import org.n1.av2.script.effect.negative.HiddenEffectsService
import org.n1.av2.script.effect.negative.SpeedUpResetTimerEffectService
import org.n1.av2.script.effect.negative.StartResetTimerEffectService
import org.n1.av2.script.effect.positive.*
import org.n1.av2.script.effect.positive.ice.*
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

enum class ScriptEffectType(
    val serviceKlass: KClass<out ScriptEffectInterface>,
) {
    DELAY_TRIPWIRE_COUNTDOWN(DelayTripwireCountdownEffectService::class),
    SCAN_ICE_NODE(ScanBeyondIceNodeEffectService::class),
    START_RESET_TIMER(StartResetTimerEffectService::class),
    SPEED_UP_RESET_TIMER(SpeedUpResetTimerEffectService::class),
    DECREASE_FUTURE_TIMERS(DecreaseFutureTimersEffectService::class),
    HIDDEN_EFFECTS(HiddenEffectsService::class),
    SITE_STATS(SiteStatsEffectService::class),
    JUMP_TO_NODE(JumpToNodeEffectService::class),
    JUMP_TO_HACKER(JumpToHackerEffectService::class),
    SWEEPER_UNBLOCK(SweeperUnblockEffectService::class),
    TANGLE_REVEAL_CLUSTERS(TangleRevealClustersEffectService::class),
    WORD_SEARCH_NEXT_WORDS(WordSearchNextWordsEffectService::class),
    AUTO_HACK_ICE_TYPE(AutoHackIceTypeEffectService::class),
    AUTO_HACK_ANY_ICE(AutoHackAnyIceEffectService::class),
    HACK_BELOW_NON_HACKED_ICE(HackBelowNonHackedIceEffectService::class),
    SHOW_MESSAGE(ShowMessageEffectService::class),
    INTERACT_WITH_SCRIPT_LAYER(InteractWithScriptLayerEffectService::class),
    AUTO_HACK_SPECIFIC_ICE_LAYER(AutoHackSpecificIceLayerEffectService::class),
    ROTATE_ICE(RotateIceEffectService::class),

}

@Service
class ScriptEffectTypeLookup(
    private val applicationContext: ApplicationContext,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun checkServicesExistAsBeans() {
        ScriptEffectType.entries.forEach {
            getForType(it)
        }
    }

    fun getForType(type: ScriptEffectType): ScriptEffectInterface {
        return applicationContext.getBean(type.serviceKlass.java)
    }
}
