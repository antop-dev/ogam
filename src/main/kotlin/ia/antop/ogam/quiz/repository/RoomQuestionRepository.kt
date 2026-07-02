package ia.antop.ogam.quiz.repository

import ia.antop.ogam.quiz.entity.RoomQuestion
import org.springframework.data.jpa.repository.JpaRepository

interface RoomQuestionRepository : JpaRepository<RoomQuestion, Long> {

    fun findByRoomIdOrderBySeq(roomId: Long): List<RoomQuestion>

    fun findByRoomIdAndSeq(roomId: Long, seq: Int): RoomQuestion?
}
