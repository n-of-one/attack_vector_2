package org.n1.av2.layer.ice.wordsearch

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class WordSearchIceController(
    val wordSearchService: WordSearchService,
    val userTaskRunner: UserTaskRunner
) {

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
