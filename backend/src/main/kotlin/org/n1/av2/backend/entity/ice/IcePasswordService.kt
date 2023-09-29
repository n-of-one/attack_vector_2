package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.nodeIdFromLayerId
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class IcePasswordService(
    private val icePasswordRepository: IcePasswordRepository,
    private val nodeEntityService: NodeEntityService,
) {

    private val generator = PasswordGenerator()

    fun getIcePassword(iceId: String): IcePassword {
        return icePasswordRepository.findByIceId(iceId) ?: createNewPassword(iceId)
    }

    fun checkIcePassword(iceId: String, passwordAttempt: String): Boolean {
        val icePassword = icePasswordRepository.findByIceId(iceId) ?: return false
        return icePassword.password == passwordAttempt.trim()
    }

    private fun createNewPassword(iceId: String): IcePassword {
        val password = createPasswordForIce(iceId)
        val id = createId("ice-password", icePasswordRepository::findById)
        val icePassword = IcePassword(id, iceId, password)
        icePasswordRepository.save(icePassword)
        return icePassword
    }

    private fun createPasswordForIce(iceId: String): String {
        val nodeId = nodeIdFromLayerId(iceId)
        val node = nodeEntityService.findById(nodeId)
        val layer = node.getLayerById(iceId)

        return generator.createPassword(layer.name)
    }

    fun deleteIcePassword(iceId: String?) {
        if (iceId == null) return
        val passwordObject = icePasswordRepository.findByIceId(iceId) ?: return
        icePasswordRepository.delete(passwordObject)
    }

}

const val PASSWORD_CHARACTERS = "abcdefghjkmnpqrstuvwyxz0123456789"
class PasswordGenerator {

    fun createPassword(iceName: String): String {
        return "${iceName}/${createSection()}/${createSection()}/"
    }

    private fun createSection(): String {
        return "${set()}-${set()}-${set()}-${set()}"
    }

    fun set(): String {
        val first = Random.nextInt(10).toString()
        val index = Random.nextInt(PASSWORD_CHARACTERS.length)
        val second = PASSWORD_CHARACTERS[index].toString()
        return first + second
    }

}