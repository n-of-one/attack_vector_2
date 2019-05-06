package org.n1.mainframe.backend.service.user

import mu.KLogging
import org.n1.mainframe.backend.model.hacker.HackerActivity
import org.n1.mainframe.backend.model.hacker.HackerActivityType
import org.n1.mainframe.backend.util.FatalException
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class HackerActivityService {

    companion object: KLogging()

    val hackerActivitiesById = HashMap<String, HackerActivity>()

    fun getActivityByUserId(userId: String): HackerActivity? {
        return hackerActivitiesById[userId]
    }

    fun getAllForScan(scanId: String): Collection<HackerActivity> {
        return hackerActivitiesById.values.filter { it.type == HackerActivityType.SCANNING && it.id == scanId }
    }

    fun startActivityOnline(principal: Principal) {
        val clientId = toClientId(principal)
        val userId = toUserId(principal)
        checkOneActivity(userId, clientId)
        hackerActivitiesById[userId] = HackerActivity(userId = userId, type = HackerActivityType.ONLINE, id = "-", clientId = clientId)
    }

    private fun checkOneActivity(userId: String, clientId: String) {
        val existingAction = hackerActivitiesById[userId]
        if (existingAction != null && existingAction.clientId != clientId) {
            throw FatalException("Please close this browser tab: can only run one activity at a time.")
        }
    }

    fun startActivityScanning(principal: Principal, scanId: String) {
        val clientId = toClientId(principal)
        val userId = toUserId(principal)
        checkOneActivity(userId, clientId)
        hackerActivitiesById[userId] = HackerActivity(userId = userId, type = HackerActivityType.SCANNING, id = scanId, clientId = clientId)
    }

    fun toClientId(principal: Principal): String {
        return split(principal)[0]
    }

    fun toUserId(principal: Principal): String {
        return split(principal)[1]
    }

    private fun split(principal: Principal): List<String> {
        val parts = principal.name.split(":")
        if (parts.size != 2) error("Invalid connectionId: ${principal.name}")
        return parts
    }

    fun endActivity(principal: Principal) {
        val clientId = toClientId(principal)
        val userId = toUserId(principal)
        val toRemove = hackerActivitiesById[userId] ?: return
        if (toRemove.clientId == clientId) {
            hackerActivitiesById.remove(userId)
        }
    }

    fun display() {
        hackerActivitiesById.values
                .sortedBy { it.clientId }
                .forEach { logger.debug { "${it.clientId}:${it.userId} - ${it.type} ${it.id}" } }
    }
}