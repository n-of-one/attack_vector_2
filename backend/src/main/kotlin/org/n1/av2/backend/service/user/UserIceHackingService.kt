package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.ice.UserIceHackingState
import org.n1.av2.backend.entity.ice.UserIceHackingStateRepository
import org.n1.av2.backend.model.iam.ConnectionType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class UserIceHackingService(
    private val repo: UserIceHackingStateRepository,
    private val stompService: StompService,
) {

    class IceHackerConnectEvent(val userId: String, val name: String)

    fun connect(userPrincipal: UserPrincipal) {
        val newState = UserIceHackingState(
            userId = userPrincipal.userId,
            connectionId = userPrincipal.connectionId,
            iceId = null,
        )

        repo.save(newState)

        class NewConnection(val connectionId: String, val type: ConnectionType)
        stompService.toUser(userPrincipal.userId, ServerActions.SERVER_USER_CONNECTION, NewConnection(userPrincipal.connectionId, ConnectionType.WS_ICE))
    }

    fun enter(userPrincipal: UserPrincipal, iceId: String) {
        val newState = UserIceHackingState(
            userId = userPrincipal.userId,
            connectionId = userPrincipal.connectionId,
            iceId = iceId,
        )

        repo.save(newState)
        stompService.toIce(
            iceId,
            ServerActions.SERVER_ICE_HACKER_CONNECTED,
            IceHackerConnectEvent(userPrincipal.userId, userPrincipal.name)
        )
    }

    fun disconnect(userPrincipal: UserPrincipal) {
        val existingStateOptional = repo.findById(userPrincipal.userId)
        val existingState = if (existingStateOptional.isEmpty) {
            return
        } else {
            existingStateOptional.get()
        }

        if (userPrincipal.connectionId != existingState.connectionId) {
            return // don't change the active session over another session disconnecting
        }

        repo.delete(existingState)

        if (existingState.iceId == null) return
        stompService.toIce(
            existingState.iceId,
            ServerActions.SERVER_ICE_HACKER_DISCONNECTED,
            IceHackerConnectEvent(userPrincipal.userId, userPrincipal.name)
        )
    }

}