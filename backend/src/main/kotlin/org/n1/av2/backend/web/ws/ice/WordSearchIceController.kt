package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class WordSearchIceController(
    val wordSearchService: WordSearchService,
    val userTaskRunner: UserTaskRunner ) {

    data class EnterInput(val iceId: String)
    @MessageMapping("/ice/wordSearch/enter")
    fun enter(command: EnterInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { wordSearchService.enter(command.iceId) }
    }

    data class SelectedInput(val iceId: String, val letters: List<String>)
    @MessageMapping("/ice/wordSearch/selected")
    fun selected(command: SelectedInput, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { wordSearchService.selected(command.iceId, command.letters) }
    }

}