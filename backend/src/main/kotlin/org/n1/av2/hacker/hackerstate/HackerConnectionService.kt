package org.n1.av2.hacker.hackerstate

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ConnectionType.WS_HACKER_MAIN
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.run.RunService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

@Configuration
class HackerConnectionServiceInit(
    val hackerConnectionService: HackerConnectionService,
    val runService: RunService,
) {

    @PostConstruct
    fun postConstruct() {
        hackerConnectionService.runService = runService
    }
}

@Service
class HackerConnectionService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val connectionService: ConnectionService,
) {

    lateinit var runService: RunService

    private val logger = mu.KotlinLogging.logger {}

    fun browserConnect(userPrincipal: UserPrincipal) {
        if (userPrincipal.type == WS_HACKER_MAIN) {

            val hackerState = hackerStateEntityService.retrieve(userPrincipal.userId)
            if (hackerState.activity.inRun) {
                runService.leaveSite(hackerState, false)
            }
            hackerStateEntityService.login(userPrincipal.userId)
        }
        else {
            // userPrincipal.type == WS_HACKER_MAIN
            hackerStateEntityService.setConnectionForNetworkedApp(userPrincipal)
        }

        // Tell browser tabs that a new connection is started and existing connections should be closed
        connectionService.toUser(
            userPrincipal.userId, ServerActions.SERVER_USER_CONNECTION,
            "connectionId" to userPrincipal.connectionId, "type" to userPrincipal.type
        )
    }


    fun sendTime() {
        connectionService.reply(ServerActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    fun browserDisconnect(userPrincipal: UserPrincipal) {
        val hackerState = hackerStateEntityService.retrieve(userPrincipal.userId)

        if (userPrincipal.type == WS_HACKER_MAIN) {
            if (hackerState.connectionId != userPrincipal.connectionId) {
                return // don't change the active session over another session disconnecting
            }

            hackerStateEntityService.goOffline(userPrincipal)

            if (hackerState.activity.inRun) {
                runService.leaveSite(hackerState, false)
            }
        } else {
            // userPrincipal.type == WS_HACKER_MAIN
            if (hackerState.networkedConnectionId != userPrincipal.connectionId) {
                return // don't change the active session over another session disconnecting
            }
            if (hackerState.networkedAppId == null) {
                return // weird, should have a networked app id
            }
            hackerStateEntityService.leaveNetworkedApp(userPrincipal)
            runService.updateIceHackers(hackerState.runId!!, hackerState.networkedAppId)
        }
    }

}
