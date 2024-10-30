package org.n1.av2.larp.frontier

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.run.RunService
import org.n1.av2.run.runlink.RunLinkService
import org.springframework.stereotype.Service

const val LOLA_USER_NAME = "LOLA"

@Service
class LolaService(
    private val connectionService: ConnectionService,
    private val runLinkService: RunLinkService,
    private val runService: RunService,
    private val hackerEntityService: HackerEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    ) {

    fun share(lolaUser: UserEntity, runId: String) {
        val lolaHackerState = hackerStateEntityService.retrieve(lolaUser.id)
        if (lolaHackerState.activity == HackerActivity.OFFLINE) {
            connectionService.replyTerminalReceive("[info]${lolaUser.name}[/] is not connected to the network.")
            return
        }

        runLinkService.shareRun(runId, lolaUser, false)

        if (lolaHackerState.runId == runId) {
            connectionService.replyTerminalReceive("[info]${lolaUser.name}[/] is already in this run.")
            return
        }
        runService.enterRun(lolaUser.id, runId, lolaHackerState.connectionId)
        connectionService.replyTerminalReceive("Called [info]${lolaUser.name}[/] to this run.")
    }

    fun createLolaUser() {
        if (userEntityService.findByNameIgnoreCase(LOLA_USER_NAME) != null) return
        val lolaUser = userEntityService.createUser(LOLA_USER_NAME, UserType.HACKER)
        hackerEntityService.createHacker(lolaUser.id, HackerIcon.BRAIN, LOLA_USER_NAME, emptyList())
    }

    class SpeakResponse(val success: Boolean, val message: String)
    fun speak(text: String): SpeakResponse {
        val lolaUser = userEntityService.getByName(LOLA_USER_NAME)

        val lolaHackerState = hackerStateEntityService.retrieve(lolaUser.id)
        if (lolaHackerState.activity == HackerActivity.OFFLINE) {
            return SpeakResponse(false, "${lolaUser.name} is not connected to the network.")
        }

        connectionService.toUser(lolaUser.id, ServerActions.SERVER_SPEAK, text)
        return SpeakResponse(true, "Speaking: ${text}")
    }
}
