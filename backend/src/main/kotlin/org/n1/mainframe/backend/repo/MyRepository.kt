package org.n1.mainframe.backend.repo

import org.springframework.transaction.annotation.Transactional
import java.io.Serializable


internal class MyRepositoryImpl<T, ID : Serializable>(entityInformation: JpaEntityInformation,
                                                      private val entityManager: EntityManager)// Keep the EntityManager around to used from the newly introduced methods.
    : SimpleJpaRepository<T, ID>(entityInformation, entityManager) {

    @Transactional
    fun <S : T> save(entity: S): S {
        // implementation goes here
    }
}