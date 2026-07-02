package ia.antop.ogam.quiz.dto

data class CreateRoomResponseDto(
    val roomCode: String,
    val playerId: String,
)

data class JoinRoomResponseDto(
    val playerId: String,
    val roomCode: String,
)

data class RoomStatusDto(
    val status: String,
    val playerCount: Int,
    val myReady: Boolean,
    val readyCount: Int,
    val currentSeq: Int,
    val currentQuestion: QuestionDto?,
    val myAnsweredCurrentSeq: Boolean,
)

data class ReadyRequestDto(
    val playerId: String,
)

data class ReadyResponseDto(
    val allReady: Boolean,
)
