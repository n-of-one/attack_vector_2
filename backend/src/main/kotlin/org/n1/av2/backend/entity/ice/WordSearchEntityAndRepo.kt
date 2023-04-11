package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

data class WordSearchStatus(
    val id: String,
    val runId: String,
    val nodeId: String,
    val layerId: String,
    val strength: IceStrength,
    val words: List<String>,
    val letters: List<List<Char>>,
    val lettersCorrect: List<String>,
    val wordIndex: Int = 0,
    val hacked: Boolean = false
)

@Repository
interface WordSearchStatusRepo: CrudRepository<WordSearchStatus, String> {
}