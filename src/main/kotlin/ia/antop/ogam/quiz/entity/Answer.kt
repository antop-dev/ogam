package ia.antop.ogam.quiz.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "answer")
class Answer(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "room_id", nullable = false)
    var roomId: Long,

    @Column(name = "player_id", nullable = false)
    var playerId: String,

    @Column(nullable = false)
    var seq: Int,

    @Column(nullable = false)
    var choice: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: String = LocalDateTime.now().toString(),
)
