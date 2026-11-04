package org.n1.av2.layer.other.daemon

import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service

@Service
class DaemonService(
    private val connectionService: ConnectionService,
    private val daemonStatusRepository: DaemonStatusRepository,
    private val currentUserService: CurrentUserService,
    private val skillService: SkillService,
) {

    fun hack() {
        connectionService.replyTerminalReceive("Hacking this layer reveals nothing new.")
    }

    fun hackerArrivesNode(siteId: String, layer: DaemonLayer) {
        val userId = currentUserService.userId
        val requiredTrait = layer.requiredTrait
        if (requiredTrait != null && !skillService.userHasTrait(userId, requiredTrait)) return

        val status = daemonStatusRepository.findByLayerId(layer.id)
        val triggeredUserIds = status?.triggeredUserIds ?: emptyList()
        if (!layer.recurring.triggers(triggeredUserIds, userId)) return

        if (!triggeredUserIds.contains(userId)) {
            val updatedStatus = status?.copy(triggeredUserIds = triggeredUserIds + userId)
                ?: DaemonStatusEntity(
                    id = createId("daemonStatus", daemonStatusRepository::findById),
                    siteId = siteId,
                    layerId = layer.id,
                    triggeredUserIds = listOf(userId),
                )
            daemonStatusRepository.save(updatedStatus)
        }

        connectionService.replyTerminalReceive(layer.text.lines())
    }

    fun siteReset(siteId: String) {
        daemonStatusRepository.deleteBySiteId(siteId)
    }
}
