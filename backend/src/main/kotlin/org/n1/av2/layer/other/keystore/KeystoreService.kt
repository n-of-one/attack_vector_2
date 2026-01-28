package org.n1.av2.layer.other.keystore

import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.util.createId
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class KeystoreService(
    private val icePasswordRepository: IcePasswordRepository,
    private val nodeEntityService: NodeEntityService,
    @Lazy private val iceService: IceService  // avoid circular dependency
) {

    private val generator = PasswordGenerator()

    fun getIcePassword(iceLayerId: String): IcePassword {
        val node = nodeEntityService.findByLayerId(iceLayerId)
        val layer = node.getLayerById(iceLayerId) as IceLayer
        val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)

        return icePasswordRepository.findByIceId(iceId) ?: createNewPassword(iceId, layer)
    }

    fun checkIcePassword(iceId: String, passwordAttempt: String): Boolean {
        val icePassword = icePasswordRepository.findByIceId(iceId) ?: return false
        return icePassword.password == passwordAttempt.trim()
    }

    private fun createNewPassword(iceId: String, layer: Layer): IcePassword {
        val password = generator.createPassword(layer.name)
        val id = createId("ice-password", icePasswordRepository::findById)
        val icePassword = IcePassword(id, iceId, password)
        icePasswordRepository.save(icePassword)
        return icePassword
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
