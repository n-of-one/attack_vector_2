package org.n1.av2.backend.db

import com.github.mongobee.changeset.ChangeLog
import com.github.mongobee.changeset.ChangeSet
import com.mongodb.DB


@ChangeLog(order = "001")
class ChangeLog_1 {

    @ChangeSet(order = "001", id = "001", author = "ErikAadVisser")
    fun BaseLine(db: DB) {
        // This sets the baseline, based on release version 005 of the code.
    }

}