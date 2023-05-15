package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.ice.UserIceHackingState
import org.n1.av2.backend.entity.ice.UserIceHackingStateRepository
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.iam.ConnectionType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class UserIceHackingService(
    private val repo: UserIceHackingStateRepository,
    private val stompService: StompService,
    private val userEntityService: UserEntityService,
) {


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

    fun enter(iceId: String) {
        val userPrincipal = UserPrincipal.fromContext()
        val newState = UserIceHackingState(
            userId = userPrincipal.userId,
            connectionId = userPrincipal.connectionId,
            iceId = iceId,
        )

        repo.save(newState)
        updateIceHackers(iceId, userPrincipal)
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

        // can only happen if disconnect is called before enter
        // this might technically happen if user closes their browser tab very quickly after opening.
        if (existingState.iceId == null) return

        updateIceHackers(existingState.iceId, userPrincipal)
    }


    class IceHacker(val userId: String, val name: String, val icon: HackerIcon)
    fun updateIceHackers(iceId: String, userPrincipal: UserPrincipal) {
        val usersInIce = repo.findByIceId(iceId)

        val iceHackers = usersInIce.map { userIceHackingState ->
            val user = userEntityService.getById(userIceHackingState.userId)
            IceHacker(user.id, user.name, user.hacker!!.icon)
        }

        stompService.toIce(
            iceId,
            ServerActions.SERVER_ICE_HACKERS_UPDATED,
            iceHackers
        )

    }

}