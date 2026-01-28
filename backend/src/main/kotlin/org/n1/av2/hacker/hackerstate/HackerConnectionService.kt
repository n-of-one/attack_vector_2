package org.n1.av2.hacker.hackerstate

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.run.RunService
import org.n1.av2.statistics.IceStatisticsService
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
    private val statisticsService: IceStatisticsService,
) {

    lateinit var runService: RunService

    fun browserConnectHackerMain(userPrincipal: UserPrincipal) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userPrincipal.userId)
        if (hackerState.activity.inRun) {
            runService.leaveSite(hackerState, false)
        }
        hackerStateEntityService.login(userPrincipal.userId)
        sendConnectionEventToAllBrowsers(userPrincipal)
    }

    fun browserConnectApp(userPrincipal: UserPrincipal) {
        hackerStateEntityService.setConnectionForNetworkedApp(userPrincipal)
        sendConnectionEventToAllBrowsers(userPrincipal)
    }

    // Tell browser tabs that a new connection is started and existing connections should be closed
    private fun sendConnectionEventToAllBrowsers(userPrincipal: UserPrincipal) {
        connectionService.toUser(
            userPrincipal.userId, ServerActions.SERVER_USER_CONNECTION,
            "connectionId" to userPrincipal.connectionId, "type" to userPrincipal.type
        )
    }


    fun sendTime() {
        connectionService.reply(ServerActions.SERVER_TIME_SYNC, ZonedDateTime.now())
    }

    fun browserDisconnectHackerMain(userPrincipal: UserPrincipal) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userPrincipal.userId)

        if (hackerState.connectionId != userPrincipal.connectionId) {
            return // don't change the active session over another session disconnecting
        }

        hackerStateEntityService.goOffline(userPrincipal)

        if (hackerState.activity.inRun) {
            runService.leaveSite(hackerState, false)
        }
    }

    fun browserDisconnectHackerApp(userPrincipal: UserPrincipal) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userPrincipal.userId)

        if (hackerState.networkedConnectionId != userPrincipal.connectionId) {
            return // don't change the active session over another session disconnecting
        }
        if (hackerState.iceId == null) {
            return // Disconnect from non-ICE
        }
        hackerStateEntityService.leaveIce(userPrincipal)
        runService.updateIceHackers(hackerState.runId!!, hackerState.iceId)
        statisticsService.hackerLeaveIce(hackerState, userPrincipal.userEntity)
    }

}
