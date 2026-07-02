package ia.antop.ogam.quiz.event

import ia.antop.ogam.quiz.service.SseEmitterService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class QuizEventHandler(
    private val sseEmitterService: SseEmitterService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onPlayerJoined(event: PlayerJoinedEvent) {
        log.debug("SSE PLAYER_JOINED: roomCode={}", event.roomCode)
        sseEmitterService.emit(event.roomCode, "PLAYER_JOINED", mapOf("playerCount" to 2))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onPlayerReady(event: PlayerReadyEvent) {
        log.debug("SSE PLAYER_READY: roomCode={}, readyCount={}", event.roomCode, event.readyCount)
        sseEmitterService.emit(event.roomCode, "PLAYER_READY", mapOf("readyCount" to event.readyCount))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onQuizStarted(event: QuizStartedEvent) {
        log.debug("SSE QUIZ_STARTED: roomCode={}", event.roomCode)
        sseEmitterService.emit(event.roomCode, "QUIZ_STARTED", mapOf(
            "seq" to event.seq,
            "optionA" to event.optionA,
            "optionB" to event.optionB,
        ))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onOpponentAnswered(event: OpponentAnsweredEvent) {
        log.debug("SSE OPPONENT_ANSWERED: roomCode={}, seq={}", event.roomCode, event.seq)
        sseEmitterService.emit(event.roomCode, "OPPONENT_ANSWERED", mapOf("seq" to event.seq))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onQuestionAdvanced(event: QuestionAdvancedEvent) {
        log.debug("SSE QUESTION_ADVANCED: roomCode={}, seq={}", event.roomCode, event.seq)
        sseEmitterService.emit(event.roomCode, "QUESTION_ADVANCED", mapOf(
            "seq" to event.seq,
            "optionA" to event.optionA,
            "optionB" to event.optionB,
        ))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onQuizCompleted(event: QuizCompletedEvent) {
        log.debug("SSE QUIZ_COMPLETED: roomCode={}", event.roomCode)
        sseEmitterService.emit(event.roomCode, "QUIZ_COMPLETED", emptyMap<String, Any>())
    }
}
