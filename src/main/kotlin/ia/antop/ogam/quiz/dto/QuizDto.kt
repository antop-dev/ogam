package ia.antop.ogam.quiz.dto

data class QuestionDto(
    val seq: Int,
    val optionA: String,
    val optionB: String,
)

data class SubmitAnswerRequestDto(
    val playerId: String,
    val seq: Int,
    val choice: String,
)

data class SubmitAnswerResponseDto(
    val recorded: Boolean,
)

data class QuestionResultDto(
    val seq: Int,
    val optionA: String,
    val optionB: String,
    val myChoice: String?,
    val opponentChoice: String?,
    val matched: Boolean,
)

data class ResultDto(
    val matchRate: Int,
    val questions: List<QuestionResultDto>,
)
