package org.n1.av2.backend.service.user

import mu.KLogging
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.hacker.HackerActivity
import org.n1.av2.backend.model.hacker.HackerActivityType
import org.n1.av2.backend.model.hacker.HackerPresence
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service


val runActivities = listOf(HackerActivityType.SCANNING, HackerActivityType.HACKING)

@Service
class HackerActivityService(
        val currentUserService: CurrentUserService
) {

    companion object: KLogging()

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    lateinit var stompService: StompService

    fun currentActivity(): HackerActivityType  {
        return hackerActivitiesById[currentUserService.userId]!!.type
    }

    fun getAll(runId: String): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { runActivities.contains(it.type) && it.runId == runId }
    }

    fun connect(userPrincipal: UserPrincipal) {
        val existingAction = hackerActivitiesById[userPrincipal.user.id]
        if (existingAction == null) {
            hackerActivitiesById[userPrincipal.user.id] = HackerActivity(user = userPrincipal.user, type = HackerActivityType.ONLINE, runId = "-")
        }
        else {
            userPrincipal.invalidate()
            logger.warn("Rejected duplicate session from: " + userPrincipal.user.name )
        }
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        val activity = hackerActivitiesById.remove(userPrincipal.user.id)!!
        if (activity.type == HackerActivityType.SCANNING || activity.type == HackerActivityType.HACKING) {
            notifyLeaveRun(activity.runId, activity.user)
        }
    }

    fun startActivityScanning(runId: String) {
        val user = currentUserService.user
        hackerActivitiesById[user.id] = HackerActivity(user, HackerActivityType.SCANNING, runId)
    }

    fun startActivityHacking(runId: String) {
        val user = currentUserService.user
        hackerActivitiesById[user.id] = HackerActivity(user, HackerActivityType.HACKING, runId)
    }

    fun stopActivityScanning(runId: String) {
        val user = currentUserService.user
        hackerActivitiesById[user.id] = HackerActivity(user , HackerActivityType.ONLINE, "-")
        notifyLeaveRun(runId, user)
    }

    fun notifyLeaveRun(runId: String, user: User) {
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_LEAVE_SCAN, HackerPresence(user.id, user.name, user.icon))
    }

}