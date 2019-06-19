package org.n1.av2.backend.model.ui

import org.n1.av2.backend.service.ReduxActions

class ReduxEvent(val type: ReduxActions,
                 val data: Any? = null)
