package org.n1.av2.backend.service.user

import mu.KLogging
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.hacker.HackerActivity
import org.n1.av2.backend.model.hacker.HackerActivityType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


val runActivities = listOf(HackerActivityType.SCANNING, HackerActivityType.HACKING)

@Service
class HackerActivityService(
        private val currentUserService: CurrentUserService,
        private val userService: UserService) {

    companion object: KLogging()

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    lateinit var stompService: StompService

    fun currentActivity(): HackerActivityType  {
        return hackerActivitiesById[currentUserService.userId]!!.type
    }

    fun getAll(runId: String, activity: HackerActivityType): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { activity == it.type && it.runId == runId }
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

    fun startActivityHacking(userId: String, runId: String) {
        val user = userService.getById(userId)
        hackerActivitiesById[userId] = HackerActivity(user, HackerActivityType.HACKING, runId)
    }

    fun stopActivityScanning(runId: String) {
        val user = currentUserService.user
        hackerActivitiesById[user.id] = HackerActivity(user , HackerActivityType.ONLINE, "-")
        notifyLeaveRun(runId, user)
    }

    fun notifyLeaveRun(runId: String, user: User) {
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_LEAVE_SCAN, HackerLeaveNotification(user.id))
    }

    fun sendTime(userId: String) {
        stompService.toUser(userId, ReduxActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    data class HackerLeaveNotification(val userId: String)

}