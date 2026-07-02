package ia.antop.ogam.quiz.repository

import ia.antop.ogam.quiz.entity.Player
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerRepository : JpaRepository<Player, String> {

    fun findByRoomId(roomId: Long): List<Player>

    fun findByIdAndRoomId(id: String, roomId: Long): Player?
}
