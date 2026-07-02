package ia.antop.ogam.quiz.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class RoomStatus { WAITING, JOINED, IN_PROGRESS, COMPLETED }

@Entity
@Table(name = "room")
class Room(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var code: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RoomStatus = RoomStatus.WAITING,

    @Column(name = "current_seq", nullable = false)
    var currentSeq: Int = 0,

    @Column(name = "created_at", nullable = false)
    var createdAt: String = LocalDateTime.now().toString(),
)
