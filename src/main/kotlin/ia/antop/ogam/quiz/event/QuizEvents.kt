package ia.antop.ogam.quiz.event

data class PlayerJoinedEvent(val roomCode: String)

data class PlayerReadyEvent(val roomCode: String, val readyCount: Int)

data class QuizStartedEvent(
    val roomCode: String,
    val seq: Int,
    val optionA: String,
    val optionB: String,
)

data class OpponentAnsweredEvent(val roomCode: String, val seq: Int)

data class QuestionAdvancedEvent(
    val roomCode: String,
    val seq: Int,
    val optionA: String,
    val optionB: String,
)

data class QuizCompletedEvent(val roomCode: String)
