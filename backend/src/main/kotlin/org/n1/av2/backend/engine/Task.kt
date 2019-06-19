package org.n1.av2.backend.engine

import java.security.Principal

data class Task(val action: () -> Unit, val principal: Principal)