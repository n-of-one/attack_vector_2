package org.n1.av2.platform.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException : RuntimeException()

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException : RuntimeException()

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException : RuntimeException()



