package ia.antop.ogam.quiz.controller

import ia.antop.ogam.quiz.dto.*
import ia.antop.ogam.quiz.service.QuizService
import ia.antop.ogam.quiz.service.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@RestController
@RequestMapping("/api/rooms")
class RoomApiController(
    private val roomService: RoomService,
    private val quizService: QuizService,
) {
    // Per-room lock: ensures ready/answer writes are serialized so concurrent
    // requests don't both read stale state and both miss the all-ready/all-answered condition.
    private val roomLocks = ConcurrentHashMap<String, ReentrantLock>()
    private fun roomLock(code: String) = roomLocks.computeIfAbsent(code) { ReentrantLock() }

    @PostMapping
    fun createRoom(): ResponseEntity<CreateRoomResponseDto> =
        ResponseEntity.ok(roomService.createRoom())

    @PostMapping("/{code}/join")
    fun joinRoom(
        @PathVariable code: String,
    ): ResponseEntity<JoinRoomResponseDto> =
        ResponseEntity.ok(roomService.joinRoom(code))

    @GetMapping("/{code}/status")
    fun getStatus(
        @PathVariable code: String,
        @RequestParam playerId: String,
    ): ResponseEntity<RoomStatusDto> =
        ResponseEntity.ok(roomService.getRoomStatus(code, playerId))

    @PostMapping("/{code}/ready")
    fun markReady(
        @PathVariable code: String,
        @RequestBody body: ReadyRequestDto,
    ): ResponseEntity<ReadyResponseDto> =
        roomLock(code).withLock {
            ResponseEntity.ok(roomService.markReady(code, body.playerId))
        }

    @PostMapping("/{code}/answers")
    fun submitAnswer(
        @PathVariable code: String,
        @RequestBody body: SubmitAnswerRequestDto,
    ): ResponseEntity<SubmitAnswerResponseDto> =
        roomLock(code).withLock {
            ResponseEntity.ok(quizService.submitAnswer(code, body.playerId, body.seq, body.choice))
        }

    @GetMapping("/{code}/results")
    fun getResults(
        @PathVariable code: String,
        @RequestParam playerId: String,
    ): ResponseEntity<ResultDto> =
        ResponseEntity.ok(quizService.getResults(code, playerId))
}
