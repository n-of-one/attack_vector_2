package org.n1.mainframe.backend.engine

import java.security.Principal

data class Task(val action: () -> Unit, val principal: Principal)