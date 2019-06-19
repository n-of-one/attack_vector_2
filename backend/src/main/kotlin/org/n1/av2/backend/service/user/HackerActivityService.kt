package org.n1.av2.backend.service.user

import mu.KLogging
import org.n1.av2.backend.model.hacker.HackerActivity
import org.n1.av2.backend.model.hacker.HackerActivityType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.PrincipalService
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
        hackerActivitiesById.remove(userPrincipal.user.id)
    }

    fun startActivityScanning(runId: String) {
        val userPrincipal = principalService.get()
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.SCANNING, id = runId)

    }

    fun stopActivityScanning(runId: String) {
        val userPrincipal = principalService.get()
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.ONLINE, id = "-")
    }

}