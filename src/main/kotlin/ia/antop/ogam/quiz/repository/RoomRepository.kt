package ia.antop.ogam.quiz.repository

import ia.antop.ogam.quiz.entity.Room
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Long> {

    fun findByCode(code: String): Room?
}
