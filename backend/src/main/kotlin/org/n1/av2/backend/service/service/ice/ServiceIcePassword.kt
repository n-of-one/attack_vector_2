package org.n1.av2.backend.service.service.ice

import org.n1.av2.backend.model.db.run.IcePasswordStatus
import org.n1.av2.backend.model.db.service.IcePasswordService
import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.service.TextService
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.ice.IcePasswordGame
import org.n1.av2.backend.service.site.NodeService

@org.springframework.stereotype.Service
class ServiceIcePassword(
        val stompService: StompService,
        val nodeService: NodeService,
        val icePasswordGame: IcePasswordGame
) {

    private data class Update(
            val serviceId: String,
            val nodeId: String,
            val lockedUntil: Long,
            val attempts: List<String>
    )

    fun hack(orig: Service, node: Node, runId: String) {
        val service = orig as IcePasswordService
        val holder = icePasswordGame.getOrCreateStatusHolder(service.id, runId)
        if (holder.hacked) {
            stompService.terminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        val status = holder.status as IcePasswordStatus
        val lockedUntil = status.lockedUntil?.toInstant()?.toEpochMilli() ?: 0
        val start = Update(service.id, node.id, lockedUntil, status.attempts )
        stompService.toRun(runId, ReduxActions.SERVER_START_HACKING_ICE_PASSWORD, start)
    }


}