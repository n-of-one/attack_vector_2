package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class WordSearchIceController(
    val wordSearchService: WordSearchService,
    val taskRunner: TaskRunner ) {

    data class EnterInput(val iceId: String)
    @MessageMapping("/ice/wordSearch/enter")
    fun enter(command: EnterInput, principal: Principal) {
        taskRunner.runTask(principal) { wordSearchService.enter(command.iceId) }
    }

    data class SelectedInput(val iceId: String, val letters: List<String>)
    @MessageMapping("/ice/wordSearch/selected")
    fun selected(command: SelectedInput, principal: Principal) {
        taskRunner.runTask(principal) { wordSearchService.selected(command.iceId, command.letters) }
    }

}