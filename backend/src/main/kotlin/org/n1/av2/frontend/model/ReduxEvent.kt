package org.n1.av2.frontend.model

import org.n1.av2.platform.connection.ServerActions

class ReduxEvent(val type: ServerActions,
                 val data: Any? = null)
