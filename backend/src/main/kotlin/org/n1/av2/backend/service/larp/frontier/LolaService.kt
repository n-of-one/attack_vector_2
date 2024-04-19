package org.n1.av2.backend.service.larp.frontier

import org.n1.av2.backend.entity.run.HackerActivity
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class LolaService(
    private val stompService: StompService,
    private val runLinkService: RunLinkService,
    private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    ) {

    fun share(lolaUser: UserEntity, runId: String) {
        val lolaHackerState = hackerStateEntityService.retrieve(lolaUser.id)
        if (lolaHackerState.activity == HackerActivity.OFFLINE) {
            stompService.replyTerminalReceive("[info]${lolaUser.name}[/] is not connected to the network.")
            return
        }

        runLinkService.shareRun(runId, lolaUser, false)

        if (lolaHackerState.runId == runId) {
            stompService.replyTerminalReceive("[info]${lolaUser.name}[/] is already in this run.")
            return
        }
        runService.enterRun(lolaUser.id, runId, lolaHackerState.connectionId)
        stompService.replyTerminalReceive("Called [info]${lolaUser.name}[/] to this run.")
    }

    fun createLolaUser() {
        userEntityService.createDefaultUser("LOLA", UserType.HACKER, HackerIcon.BRAIN)
    }

    class SpeakResponse(val success: Boolean, val message: String)

    fun speak(text: String): SpeakResponse {
        val lolaUser = userEntityService.getByName("LOLA")

        val lolaHackerState = hackerStateEntityService.retrieve(lolaUser.id)
        if (lolaHackerState.activity == HackerActivity.OFFLINE) {
            return SpeakResponse(false, "${lolaUser.name} is not connected to the network.")
        }


        stompService.toUser(lolaUser.id, ServerActions.SERVER_SPEAK, text)
        return SpeakResponse(true, "Speaking: ${text}")
    }
}
