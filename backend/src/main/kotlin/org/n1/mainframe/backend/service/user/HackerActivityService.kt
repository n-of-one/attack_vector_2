package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerActivity
import org.n1.mainframe.backend.model.hacker.HackerActivityType
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
        val clientId = toClientId(principal)
        val userName = toUserName(principal)
        checkOneActivity(userName, clientId)
        hackerActivitiesById[userName] = HackerActivity(userName = userName, type = HackerActivityType.ONLINE, id = "-", clientId = clientId)
    }

    private fun checkOneActivity(userId: String, clientId: String) {
        val existingAction = hackerActivitiesById[userId]
        if (existingAction != null && existingAction.clientId != clientId) {
            throw FatalException("Please close this browser tab, hackers can only perform one activity at a time.")
        }
    }

    fun startActivityScanning(principal: Principal, scanId: String) {
        val clientId = toClientId(principal)
        val userName = toUserName(principal)
        checkOneActivity(userName, clientId)
        hackerActivitiesById[userName] = HackerActivity(userName = userName, type = HackerActivityType.SCANNING, id = scanId, clientId = clientId)
    }

    fun toClientId(principal: Principal): String {
        return split(principal)[0]
    }

    fun toUserName(principal: Principal): String {
        return split(principal)[1]
    }

    private fun split(principal: Principal): List<String> {
        val parts = principal.name.split(":")
        if (parts.size != 2) error("Invalid connectionId: ${principal.name}")
        return parts
    }

    fun endActivity(principal: Principal) {
        val clientId = toClientId(principal)
        val userName = toUserName(principal)
        val toRemove = hackerActivitiesById[userName] ?: return
        if (toRemove.clientId == clientId) {
            hackerActivitiesById.remove(userName)
        }
    }
}