package org.n1.av2.layer.ice.wordsearch

import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class WordSearchIceStatus(
    @Id val id: String,
    @Indexed val layerId: String,
    val strength: IceStrength,
    val words: List<String>,
    val letterGrid: List<List<Char>>,
    val correctPositions: List<String>, // all letter positions of letters that were correctly found, type: "{x}:{y}"
    val wordIndex: Int = 0,
    val hacked: Boolean = false,
    val solutions: List<List<String>>, // solutions (list of letter positions "{x}:{y}" )
)

@Repository
interface WordSearchIceStatusRepo: CrudRepository<WordSearchIceStatus, String> {
    fun findByLayerId(layerId: String): WordSearchIceStatus?
}
