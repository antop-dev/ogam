package ia.antop.ogam.quiz.repository

import ia.antop.ogam.quiz.entity.Answer
import org.springframework.data.jpa.repository.JpaRepository

interface AnswerRepository : JpaRepository<Answer, Long> {

    fun findByRoomIdAndSeq(roomId: Long, seq: Int): List<Answer>

    fun findByRoomId(roomId: Long): List<Answer>

    fun existsByRoomIdAndPlayerIdAndSeq(roomId: Long, playerId: String, seq: Int): Boolean
}
