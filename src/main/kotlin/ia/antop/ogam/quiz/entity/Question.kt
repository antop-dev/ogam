package ia.antop.ogam.quiz.entity

import jakarta.persistence.*

@Entity
@Table(name = "question")
class Question(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "option_a", nullable = false)
    var optionA: String,

    @Column(name = "option_b", nullable = false)
    var optionB: String,
)
