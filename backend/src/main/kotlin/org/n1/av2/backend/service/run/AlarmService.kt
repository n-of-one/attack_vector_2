package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.GameEvent
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service


class AlarmGameEvent(val runId: String): GameEvent

@Service
class AlarmService(
        val stompService: StompService) {

    fun processAlarm(event: AlarmGameEvent) {
        stompService.toRun(event.runId, ReduxActions.SERVER_COMPLETE_COUNTDOWN)
    }


}