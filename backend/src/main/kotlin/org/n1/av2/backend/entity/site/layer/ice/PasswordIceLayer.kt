package org.n1.av2.backend.entity.site.layer.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.service.site.SiteValidationException

class PasswordIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    var password: String,
    var hint: String
) : IceLayer(id, type, level, name, note, strength, hacked) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.PASSWORD_ICE, level, defaultName, "", IceStrength.AVERAGE, false,"", "")

    @Suppress("UNUSED_PARAMETER")
    private fun validatePassword(siteRep: SiteRep) {
        if (this.password.isEmpty()) throw SiteValidationException("Password cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validatePassword)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "PASSWORD" -> password = value
            "HINT" -> hint = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}