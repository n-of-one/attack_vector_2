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
    val letterGrid: List<List<Char>>,
    val correctPositions: List<String>, // all letter positions of letters that were correctly found, type: "{x}:{y}"
    val wordIndex: Int = 0,
    val hacked: Boolean = false,
    val solutions: List<List<String>>, // solutions (list of letter positions "{x}:{y}" )
)

@Repository
interface WordSearchStatusRepo: CrudRepository<WordSearchStatus, String> {
}