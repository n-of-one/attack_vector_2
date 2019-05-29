package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

private const val PASSWORD = "password"
private const val HINT = "hint"

class IcePasswordService(
        id: String,
        type: ServiceType,
        layer: Int,
        data: MutableMap<String, String>
) : Service(id, type, layer, data) {

    constructor(id: String, layer: Int, defaultName: String) : this(id, ServiceType.ICE_PASSWORD, layer, HashMap()) {
        setDefaultName(defaultName)
    }

    val password: String
    @JsonIgnore
    get() {
        return data[PASSWORD] ?: ""
    }

    val hint: String?
        @JsonIgnore
        get() {
            return data[HINT] ?: ""
        }


    @Suppress("UNUSED_PARAMETER")
    private fun validatePassword(siteRep: SiteRep) {
        if (this.password.isEmpty()) throw ValidationException("Password cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validatePassword )
    }
}