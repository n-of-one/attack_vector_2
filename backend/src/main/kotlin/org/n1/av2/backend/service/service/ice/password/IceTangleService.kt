package org.n1.av2.backend.service.service.ice.password

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.*
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IceStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

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
        return createServiceStatus(layer, runId)

//        FIXME
//        return (iceStatusRepo.findByLayerIdAndRunId(layer.id, runId) ?: createServiceStatus(layer, runId)) as IceTangleStatus
    }

    private fun createServiceStatus(layer: Layer, runId: String): IceTangleStatus {
        val id = createId("icePasswordStatus-")

        val creation = TangleCreator().create(8)

        val iceTangleStatus = IceTangleStatus(id, layer.id, runId, creation.points, creation.points, creation.lines)
        iceStatusRepo.save(iceTangleStatus)
        return iceTangleStatus
    }

}