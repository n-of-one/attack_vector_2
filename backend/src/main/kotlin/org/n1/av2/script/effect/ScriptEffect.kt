package org.n1.av2.script.effect

sealed class ScriptEffect(
) {
    abstract val name: String
    abstract val playerDescription: String
    abstract val gmDescription: String
}

abstract class ScriptEffectWithValue(val value: String): ScriptEffect() {
    open fun copyWithNewValue(newValue: String): ScriptEffectWithValue {
        val constructor =  this::class.constructors.first()
        return constructor.call(newValue)
    }

    abstract fun validate(toValidate: String): String?
}

enum class ScriptEffectType(
    val createEffect: () -> ScriptEffect
) {
    DELAY_TRIPWIRE_COUNTDOWN({ DelayTripwireCountdown() }),
    SCAN_ICE_NODE({ ScanIceNode() }),

    START_RESET_TIMER({  StartResetTimer() }),
    SPEED_UP_RESET_TIMER({ SpeedUpResetTimer() }),
    DECREASE_FUTURE_TIMERS({  DecreaseFutureTimers() }),
}
