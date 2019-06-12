package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerActivity
import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.iam.UserPrincipal
import org.n1.mainframe.backend.service.PrincipalService
import org.springframework.stereotype.Service

@Service
class HackerActivityService(
        val principalService: PrincipalService
) {

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    fun getAll(type: HackerActivityType, id: String): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { it.type == HackerActivityType.SCANNING && it.id == id }
    }

    fun startActivityOnline(userPrincipal: UserPrincipal) {
        val existingAction = hackerActivitiesById[userPrincipal.user.id]
        if (existingAction == null) {
            hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.ONLINE, id = "-")
        }
    }

    fun startActivityScanning(scanId: String) {
        val userPrincipal = principalService.get()
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.SCANNING, id = scanId)

    }

    fun stopActivityScanning(scanId: String) {
        val userPrincipal = principalService.get()
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.ONLINE, id = "-")
    }

    fun endActivity(userPrincipal: UserPrincipal) {
        val toRemove = hackerActivitiesById[userPrincipal.user.id] ?: return
        if (toRemove.authentication.clientId == userPrincipal.clientId) {
            hackerActivitiesById.remove(userPrincipal.user.id)
        }
    }

    data class ConnectionCheckResponse(val ok: Boolean, val message: String? = null)

    fun checkConnection(userPrincipal: UserPrincipal): ConnectionCheckResponse {
        val existingAction = hackerActivitiesById[userPrincipal.user.id]
                ?: return ConnectionCheckResponse(false, "Connection not correctly established. Please refresh browser.")

        if (existingAction.authentication.clientId != userPrincipal.clientId) {
            return ConnectionCheckResponse(false, "Please close this browser tab, hackers can only use one browser tab at a time..")
        }
        return ConnectionCheckResponse(true)
    }
}