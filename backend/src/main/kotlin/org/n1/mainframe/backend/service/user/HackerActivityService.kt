package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerActivity
import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.model.iam.UserAuthentication
import org.n1.mainframe.backend.util.FatalException
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class HackerActivityService {

//    companion object: KLogging()

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    fun getAll(type: HackerActivityType, id: String): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { it.type == HackerActivityType.SCANNING && it.id == id }
    }

    fun startActivityOnline(principal: Principal) {
        val authentication  = principal as UserAuthentication
        checkOneActivity(authentication)
        hackerActivitiesById[authentication.user.id] = HackerActivity(authentication = authentication , type = HackerActivityType.ONLINE, id = "-")
    }

    private fun checkOneActivity(authentication: UserAuthentication) {
        val existingAction = hackerActivitiesById[authentication.user.id]
        if (existingAction != null && existingAction.authentication.clientId != authentication.clientId) {
            throw FatalException("Please close this browser tab, hackers can only perform one activity at a time.")
        }
    }

    fun startActivityScanning(principal: Principal, scanId: String) {
        val authentication  = principal as UserAuthentication
        checkOneActivity(authentication)
        hackerActivitiesById[authentication.user.id] = HackerActivity(authentication = authentication, type = HackerActivityType.SCANNING, id = scanId)
    }

    fun endActivity(principal: Principal) {
        val authentication  = principal as UserAuthentication

        val toRemove = hackerActivitiesById[authentication.user.id] ?: return
        if (toRemove.authentication.clientId == authentication.clientId) {
            hackerActivitiesById.remove(authentication.user.id)
        }
    }
}