package org.n1.av2.backend.service.run.ice


@org.springframework.stereotype.Service
class IcePasswordService(

) {


    class SubmitPassword(val serviceId: String, val nodeId: String, val runId: String, val password: String, val now: Long, val offsetMinutes: Int)

    fun submitPassword(submit: SubmitPassword) {

    }
}