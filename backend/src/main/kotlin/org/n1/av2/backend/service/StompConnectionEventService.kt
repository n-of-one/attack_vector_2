package org.n1.av2.backend.service

import mu.KLogging
import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.UserConnectionService
import org.springframework.stereotype.Service

@Service
class StompConnectionEventService {


    lateinit var executor: SerializingExecutor
    lateinit var userConnectionService: UserConnectionService

    companion object : KLogging()


    // FIXME cleanup - remove if not needed
//
//    fun currentActivity(): HackerActivityType  {
//        return hackerActivitiesById[currentUserService.userId]!!.type
//    }
//
//    fun getAll(runId: String, activity: HackerActivityType): Collection<HackerActivity> {
//        return hackerActivitiesById.values.filter { activity == it.type && it.runId == runId }
//    }
//

    //
//    fun startActivityScanning(runId: String) {
//        val user = currentUserService.user
//        hackerActivitiesById[user.id] = HackerActivity(user, HackerActivityType.SCANNING, runId)
//    }
//
//    fun startActivityHacking(userId: String, runId: String) {
//        val user = userService.getById(userId)
//        hackerActivitiesById[userId] = HackerActivity(user, HackerActivityType.HACKING, runId)
//    }
//
//    fun stopActivityScanning(runId: String) {
//        val user = currentUserService.user
//        hackerActivitiesById[user.id] = HackerActivity(user , HackerActivityType.ONLINE, "-")
//        notifyLeaveRun(runId, user)
//    }

//    data class HackerLeaveNotification(val userId: String)



    /** Returns validity of connection. False means this is a duplicate connection */
    fun connect(userPrincipal: UserPrincipal): Boolean {

        // run immediate because we need to immediately check if this is a duplicate connection,
        // and report that back to on this connection request
        return userConnectionService.connect(userPrincipal)
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        executor.run(userPrincipal) {
            userConnectionService.disconnect(userPrincipal)
        }
    }

    fun sendTime(principal: UserPrincipal) {
        executor.run(principal) {
            userConnectionService.sendTime()
        }
    }

}