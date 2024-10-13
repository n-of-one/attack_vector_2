package org.n1.av2.platform.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/*
For all config that is static, i.e. fixed at startup
Minimize usage to connection to database
*/
@Service
class StaticConfig(

    @Value("\${MONGODB_NAME:attackvector2}")
    val mongoDbName: String,

    @Value("\${MONGODB_URI:mongodb://attackvector2:attackvector2@localhost/admin?authMechanism=SCRAM-SHA-1}")
    val mongoDbUrl: String,

    // Needed to set up the date/time convertors for MongoDB. Could theoretically be supported to change at runtime, but this would introduce a lot of complexity
    @Value("\${environment.TIME_ZONE:default}")
    val timeZoneInput: String,

)
