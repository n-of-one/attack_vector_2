package org.n1.av2.backend.model.db.service

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.model.ui.ValidationException

private const val PASSWORD = "password"
private const val HINT = "hint"

class IcePasswordService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String,
        hacked: Boolean,
        var password: String,
        var hint: String

) : IceService(id, type, layer, name, note, hacked) {

    constructor(id: String, layer: Int, defaultName: String) :
            this(id, ServiceType.ICE_PASSWORD, layer, defaultName, "", false, "", "")



    @Suppress("UNUSED_PARAMETER")
    private fun validatePassword(siteRep: SiteRep) {
        if (this.password.isEmpty()) throw ValidationException("Password cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validatePassword)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            PASSWORD -> password = value
            HINT -> hint = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}