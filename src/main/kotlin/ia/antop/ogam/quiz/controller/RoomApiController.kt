package ia.antop.ogam.quiz.controller

import ia.antop.ogam.quiz.dto.*
import ia.antop.ogam.quiz.service.QuizService
import ia.antop.ogam.quiz.service.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rooms")
class RoomApiController(
    private val roomService: RoomService,
    private val quizService: QuizService,
) {

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
        ResponseEntity.ok(roomService.markReady(code, body.playerId))

    @PostMapping("/{code}/answers")
    fun submitAnswer(
        @PathVariable code: String,
        @RequestBody body: SubmitAnswerRequestDto,
    ): ResponseEntity<SubmitAnswerResponseDto> =
        ResponseEntity.ok(quizService.submitAnswer(code, body.playerId, body.seq, body.choice))

    @GetMapping("/{code}/results")
    fun getResults(
        @PathVariable code: String,
        @RequestParam playerId: String,
    ): ResponseEntity<ResultDto> =
        ResponseEntity.ok(quizService.getResults(code, playerId))
}
