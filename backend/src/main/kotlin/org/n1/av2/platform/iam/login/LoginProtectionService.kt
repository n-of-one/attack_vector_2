package org.n1.av2.platform.iam.login

import org.n1.av2.platform.util.Cache
import org.springframework.stereotype.Service
import java.time.Duration

const val FAILED_LOGINS_PER_HOUR_WARNING = 3
const val FAILED_LOGINS_PER_HOUR_MAX = 5


@Service
class LoginProtectionService {
    private val cache = Cache<Int>(Duration.ofHours(1))

    fun preventBruteForce(ipAddress: String) {
        val failedLoginsCount = cache.get(ipAddress, 0)
        if (failedLoginsCount >= FAILED_LOGINS_PER_HOUR_MAX) {
            error("Too many failed logins from this IP, try again in one hour.")
        }
    }

    fun updateFailedLogins(ipAddress: String) {
        val newFailedLoginsCount = cache.get(ipAddress, 0) + 1
        cache.set(ipAddress, newFailedLoginsCount)
    }

    fun issueWarning(ipAddress: String) {
        val failedLoginsCount = cache.get(ipAddress, 0)
        if (failedLoginsCount > FAILED_LOGINS_PER_HOUR_WARNING) {
            error("Wrong password, please look up the password. You can only try $FAILED_LOGINS_PER_HOUR_MAX times per hour.")
        }
    }

}
