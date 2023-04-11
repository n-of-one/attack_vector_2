package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.IceWordSearchService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class IceWordSearchController(
        val iceWordSearchService: IceWordSearchService,
        val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)
    @MessageMapping("/ice/wordSearch/enter")
    fun enter(command: EnterInput, principal: Principal) {
        taskRunner.runTask(principal) { iceWordSearchService.enter(command.iceId) }
    }

    data class SelectedInput(val iceId: String, val letters: List<String>)
    @MessageMapping("/ice/wordSearch/selected")
    fun selected(command: SelectedInput, principal: Principal) {
        taskRunner.runTask(principal) { iceWordSearchService.selected(command.iceId, command.letters) }
    }

}