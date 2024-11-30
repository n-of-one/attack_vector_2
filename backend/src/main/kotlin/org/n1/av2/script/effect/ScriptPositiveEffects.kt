package org.n1.av2.script.effect

import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.springframework.data.annotation.Transient

class DelayTripwireCountdown(value: String = "00:02:00") : ScriptEffectWithValue( value) {
    @Transient
    override val name = "Delay tripwire countdown"

    @Transient
    override val playerDescription = "Delay a running tripwire countdown timer by ${toHumanTime(value.toDuration())}."

    @Transient
    override val gmDescription = "When running this script in a node with a tripwire that has an active countdown timer, " +
        "this timer will increase to give the players more time."

    override fun validate(toValidate: String): String? {
        return toValidate.validateDuration()
    }
}

class ScanIceNode() : ScriptEffect() {

    @Transient
    override val name = "Scan beyond ICE node"

    @Transient
    override val playerDescription = ( "Scan beyond a node with ICE." )

    @Transient
    override val gmDescription = ( "When running this script on a node with ICE, it will scan beyond that node." )
}
