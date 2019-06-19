package org.n1.av2.backend.model.db.changelog

import com.github.mongobee.changeset.ChangeLog
import com.github.mongobee.changeset.ChangeSet
import com.mongodb.DB


@Suppress("UNUSED_PARAMETER")
@ChangeLog(order = "001")
class ChangeLog_1 {

    @ChangeSet(order = "001", id = "001", author = "ErikAadVisser")
    fun BaseLine(ignore: DB) {
        // This sets the baseline, based on release version 005 of the code.
    }

}