package ia.antop.ogam.quiz.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "player")
class Player(

    @Id
    val id: String,

    @Column(name = "room_id", nullable = false)
    var roomId: Long,

    @Column(name = "is_ready", nullable = false)
    var isReady: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: String = LocalDateTime.now().toString(),
)
