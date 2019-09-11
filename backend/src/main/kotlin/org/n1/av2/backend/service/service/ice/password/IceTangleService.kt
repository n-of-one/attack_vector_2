package org.n1.av2.backend.service.service.ice.password

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.*
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IceStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.web.ws.ice.IceTangleController
import org.springframework.stereotype.Service

const val X_SIZE = 1200
const val Y_SIZE = 680
const val PADDING = 10

@Service
class IceTangleService(
        val iceStatusRepo: IceStatusRepo,
        val stompService: StompService) {

    data class UiTangleState(
            val layerId: String,
            val points: MutableList<TanglePoint>,
            val lines: List<TangleLine>
    )

    fun hack(layer: Layer, runId: String) {
        val tangleStatus = getOrCreateStatus(layer, runId)

        val uiState = UiTangleState(layer.id, tangleStatus.points, tangleStatus.lines)
        stompService.toUser(ReduxActions.SERVER_START_HACKING_ICE_TANGLE, uiState)
    }

    private fun getOrCreateStatus(layer: Layer, runId: String): IceTangleStatus {
        return getStatus(layer.id, runId) ?: createServiceStatus(layer, runId)
    }

    private fun getStatus(layerId: String, runId: String): IceTangleStatus? {
        val tangleStatus = iceStatusRepo.findByLayerIdAndRunId(layerId, runId) ?: return null
        return tangleStatus as IceTangleStatus
    }

    private fun createServiceStatus(layer: Layer, runId: String): IceTangleStatus {
        val id = createId("icePasswordStatus-")

        val creation = TangleCreator().create(6)

        val iceTangleStatus = IceTangleStatus(id, layer.id, runId, creation.points, creation.points, creation.lines)
        iceStatusRepo.save(iceTangleStatus)
        return iceTangleStatus
    }

    // Puzzle solving //

    data class TanglePointMoved(val layerId: String, val id: Int, val x: Int, val y: Int)

    fun move(command: IceTangleController.TanglePointMoveInput) {
        val x = keepInPlayArea(command.x, X_SIZE)
        val y = keepInPlayArea(command.y, Y_SIZE)

        val tangleStatus = getStatus(command.layerId, command.runId)!!

        tangleStatus.points.removeIf { it.id == command.id}
        val newPoint = TanglePoint(command.id, x, y)
        tangleStatus.points.add(newPoint)

        iceStatusRepo.save(tangleStatus)

        val message = TanglePointMoved(command.layerId, command.id, x, y)

        stompService.toRun(command.runId, ReduxActions.SERVER_TANGLE_POINT_MOVED, message)
    }

    private fun keepInPlayArea(position: Int, size: Int): Int {
        if (position > size - PADDING) return size - PADDING
        if (position < PADDING) return PADDING
        return position
    }

}