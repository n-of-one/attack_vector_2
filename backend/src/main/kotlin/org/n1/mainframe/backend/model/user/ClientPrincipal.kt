package org.n1.mainframe.backend.model.user

import java.security.Principal

class ClientPrincipal(val user: User, val clientId: String): Principal {

    override fun getName(): String {
        return clientId + ":" + user.userName
    }
}
