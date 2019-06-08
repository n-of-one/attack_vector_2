package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerActivity
import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.hacker.HackerPresence
import org.n1.mainframe.backend.model.iam.UserPrincipal
import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.service.PrincipalService
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.util.FatalException
import org.springframework.stereotype.Service

@Service
class HackerActivityService(
        val principalService: PrincipalService
) {

//    companion object: KLogging()

    lateinit var stompService: StompService

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    fun getAll(type: HackerActivityType, id: String): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { it.type == HackerActivityType.SCANNING && it.id == id }
    }

    fun startActivityOnline(userPrincipal: UserPrincipal) {
        checkOneActivity(userPrincipal)
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal , type = HackerActivityType.ONLINE, id = "-")
    }

    private fun checkOneActivity(userPrincipal: UserPrincipal) {

        val existingAction = hackerActivitiesById[userPrincipal.user.id]
        if (existingAction != null && existingAction.authentication.clientId != userPrincipal.clientId) {
            throw FatalException("Please close this browser tab, hackers can only perform one activity at a time.")
        }
    }

    fun startActivityScanning(scanId: String) {
        val userPrincipal  = principalService.get()
        checkOneActivity(userPrincipal)
        hackerActivitiesById[userPrincipal.user.id] = HackerActivity(authentication = userPrincipal, type = HackerActivityType.SCANNING, id = scanId)

        stompService.toScan(scanId, ReduxActions.SERVER_HACKER_ENTER_SCAN, createPresence(userPrincipal.user))
    }

    fun endActivity(userPrincipal: UserPrincipal) {
        val toRemove = hackerActivitiesById[userPrincipal.user.id] ?: return
        if (toRemove.authentication.clientId == userPrincipal.clientId) {
            hackerActivitiesById.remove(userPrincipal.user.id)
        }
    }


    fun getUserPresence(type: HackerActivityType, id: String): List<HackerPresence> {
        return getAll(type, id).map{ activity -> createPresence(activity.authentication.user)}
    }

    private fun createPresence(user: User): HackerPresence {
        return HackerPresence(user.id, user.name, user.icon)
    }


}