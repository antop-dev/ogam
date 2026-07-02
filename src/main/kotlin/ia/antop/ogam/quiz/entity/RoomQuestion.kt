package ia.antop.ogam.quiz.entity

import jakarta.persistence.*

@Entity
@Table(name = "room_question")
class RoomQuestion(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "room_id", nullable = false)
    var roomId: Long,

    @Column(nullable = false)
    var seq: Int,

    @Column(name = "question_id", nullable = false)
    var questionId: Long,
)
