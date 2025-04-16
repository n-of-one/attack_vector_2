package org.n1.av2.layer.ice.password

import org.n1.av2.editor.SiteRep
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

class PasswordIceLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    var password: String,
    var hint: String
) : IceLayer(id, LayerType.PASSWORD_ICE, level, name, note, strength, hacked) {

    constructor(id: String, level: Int, name: String, note: String, strength: IceStrength, hacked: Boolean, password: String, hint: String) :
        this(id, LayerType.PASSWORD_ICE, level, name, note, strength, hacked, password, hint)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.PASSWORD_ICE, level, defaultName, "", IceStrength.AVERAGE, false, "", "")

    constructor(id: String, toClone: PasswordIceLayer) :
        this(id, LayerType.PASSWORD_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked, toClone.password, toClone.hint)

    @Suppress("unused")
    private fun validatePassword(siteRep: SiteRep) {
        if (this.password.isEmpty()) throw SiteValidationException("Password cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validatePassword)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "PASSWORD" -> password = value
            "HINT" -> hint = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}
