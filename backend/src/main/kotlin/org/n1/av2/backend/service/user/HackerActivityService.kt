package org.n1.av2.backend.service.user

import mu.KLogging
import org.n1.av2.backend.model.hacker.HackerActivity
import org.n1.av2.backend.model.hacker.HackerActivityType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.CurrentUserService
import org.springframework.stereotype.Service

@Service
class HackerActivityService(
        val currentUserService: CurrentUserService
) {

    companion object: KLogging()

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    fun currentActivity(): HackerActivityType  {
        return hackerActivitiesById[currentUserService.userId]!!.type
    }

    fun getAll(id: String, vararg type: HackerActivityType): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { type.contains(it.type) && it.id == id }
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
        val userPrincipal = currentUserService.principal
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.SCANNING, id = runId)
    }

    fun startActivityHacking(runId: String) {
        val userPrincipal = currentUserService.principal
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.HACKING, id = runId)
    }

    fun stopActivityHacking(runId: String, newActivity: HackerActivityType) {
        val userPrincipal = currentUserService.principal
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = newActivity, id = runId)
    }

    fun stopActivityScanning(runId: String) {
        val userPrincipal = currentUserService.principal
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.ONLINE, id = "-")
    }

}