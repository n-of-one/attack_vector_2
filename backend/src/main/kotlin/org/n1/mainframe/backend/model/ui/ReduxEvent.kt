package org.n1.mainframe.backend.model.ui

import org.n1.mainframe.backend.service.ReduxActions

class ReduxEvent(val type: ReduxActions,
                 val data: Any? = null)
