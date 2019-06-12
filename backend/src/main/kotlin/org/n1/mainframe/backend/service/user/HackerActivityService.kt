package org.n1.mainframe.backend.service.user

import mu.KLogging
import org.n1.mainframe.backend.model.hacker.HackerActivity
import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.iam.UserPrincipal
import org.n1.mainframe.backend.service.PrincipalService
import org.springframework.stereotype.Service

@Service
class HackerActivityService(
        val principalService: PrincipalService
) {

    companion object: KLogging()

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    fun getAll(type: HackerActivityType, id: String): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { it.type == HackerActivityType.SCANNING && it.id == id }
    }

    fun connect(userPrincipal: UserPrincipal) {
        val existingAction = hackerActivitiesById[userPrincipal.user.id]
        if (existingAction == null) {
            hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.ONLINE, id = "-")
        }
        else {
            userPrincipal.invalidate()
            logger.warn("Rejected duplicate session from: " + userPrincipal.user.name )
        }
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        val toRemove = hackerActivitiesById[userPrincipal.user.id] ?: return
        hackerActivitiesById.remove(userPrincipal.user.id)
    }

    fun startActivityScanning(scanId: String) {
        val userPrincipal = principalService.get()
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.SCANNING, id = scanId)

    }

    fun stopActivityScanning(scanId: String) {
        val userPrincipal = principalService.get()
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.ONLINE, id = "-")
    }

}