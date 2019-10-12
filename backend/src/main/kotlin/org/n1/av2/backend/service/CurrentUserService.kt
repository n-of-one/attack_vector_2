package org.n1.av2.backend.service

import org.n1.av2.backend.model.db.user.User
import org.springframework.stereotype.Service

@Service
class CurrentUserService {
    private val userStore = ThreadLocal<User>()

    fun set(user: User) {
        userStore.set(user)
    }

    val user: User
    get () {
        return userStore.get()
    }

    val userId: String
        get () {
            return userStore.get().id
        }

    fun remove() {
        userStore.remove()
    }


}