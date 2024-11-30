package org.n1.av2.script.effect

import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.springframework.data.annotation.Transient

class StartResetTimer(value: String = "00:15:00") : ScriptEffectWithValue(value) {

    @Transient
    override val name = "Drawback: Start reset timer"

    @Transient
    override val playerDescription =
        ("Start a countdown of ${toHumanTime(value.toDuration())} to reset the site.")

    @Transient
    override val gmDescription = ("Drawback: when running this script, a countdown timer will start to reset the site. ")

    override fun validate(toValidate: String): String? {
        return toValidate.validateDuration()
    }
}

class SpeedUpResetTimer(value: String = "00:10:00") : ScriptEffectWithValue(value) {

    @Transient
    override val name = "Drawback: Speed up reset timer"

    @Transient
    override val playerDescription =
        ("If there already was a reset countdown triggered by another script, it is sped up by ${toHumanTime(value.toDuration())}.")

    @Transient
    override val gmDescription = ("Drawback: when running this script, an existing countdown timer " +
        "(triggered by the 'Start reset timer') effect is sped up.<br/>\n This prevents players from running multiple " +
        "versions of the 'Start reset timer' without any consequences.")

    override fun validate(toValidate: String): String? {
        return toValidate.validateDuration()
    }
}

class DecreaseFutureTimers(value: String = "00:01:00") : ScriptEffectWithValue(value) {

    @Transient
    override val name = "Drawback: Decrease future timers"

    @Transient
    override val playerDescription =
        ("Decrease future timers by ${toHumanTime(value.toDuration())}.")

    @Transient
    override val gmDescription = ("Drawback: when running this script, future timers (tripwire or otherwise) will be shorter.")

    override fun validate(toValidate: String): String? {
        return toValidate.validateDuration()
    }
}
