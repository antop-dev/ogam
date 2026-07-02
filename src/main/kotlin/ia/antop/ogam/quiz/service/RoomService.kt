package ia.antop.ogam.quiz.service

import ia.antop.ogam.common.exception.InvalidPlayerException
import ia.antop.ogam.common.exception.RoomFullException
import ia.antop.ogam.common.exception.RoomNotFoundException
import ia.antop.ogam.quiz.dto.*
import ia.antop.ogam.quiz.entity.Player
import ia.antop.ogam.quiz.entity.Room
import ia.antop.ogam.quiz.entity.RoomQuestion
import ia.antop.ogam.quiz.entity.RoomStatus
import ia.antop.ogam.quiz.event.*
import ia.antop.ogam.quiz.repository.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val playerRepository: PlayerRepository,
    private val questionRepository: QuestionRepository,
    private val roomQuestionRepository: RoomQuestionRepository,
    private val answerRepository: AnswerRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createRoom(): CreateRoomResponseDto {
        val code = generateCode()
        val room = roomRepository.save(Room(code = code))
        val playerId = UUID.randomUUID().toString()
        playerRepository.save(Player(id = playerId, roomId = room.id!!))
        log.info("Room created: code={}, playerId={}", code, playerId)
        return CreateRoomResponseDto(roomCode = code, playerId = playerId)
    }

    @Transactional
    fun joinRoom(code: String): JoinRoomResponseDto {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException("Room not found: $code")

        if (room.status != RoomStatus.WAITING) {
            throw RoomFullException("Room is not accepting new players: $code")
        }

        val existing = playerRepository.findByRoomId(room.id!!)
        if (existing.size >= 2) throw RoomFullException("Room is full: $code")

        val playerId = UUID.randomUUID().toString()
        playerRepository.save(Player(id = playerId, roomId = room.id!!))
        room.status = RoomStatus.JOINED
        roomRepository.save(room)

        log.info("Player joined: code={}, playerId={}", code, playerId)
        eventPublisher.publishEvent(PlayerJoinedEvent(roomCode = code))

        return JoinRoomResponseDto(playerId = playerId, roomCode = code)
    }

    @Transactional
    fun markReady(code: String, playerId: String): ReadyResponseDto {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException("Room not found: $code")
        val player = playerRepository.findByIdAndRoomId(playerId, room.id!!)
            ?: throw InvalidPlayerException("Player not in room: $playerId")

        player.isReady = true
        playerRepository.save(player)

        val players = playerRepository.findByRoomId(room.id!!)
        val readyCount = players.count { it.isReady }
        val allReady = players.size == 2 && readyCount == 2

        if (allReady) {
            val questions = questionRepository.findAll().shuffled().take(10)
            questions.forEachIndexed { index, question ->
                roomQuestionRepository.save(
                    RoomQuestion(roomId = room.id!!, seq = index + 1, questionId = question.id!!)
                )
            }
            room.status = RoomStatus.IN_PROGRESS
            room.currentSeq = 1
            roomRepository.save(room)

            val first = questions.first()
            eventPublisher.publishEvent(QuizStartedEvent(code, 1, first.optionA, first.optionB))
        } else {
            eventPublisher.publishEvent(PlayerReadyEvent(code, readyCount))
        }

        return ReadyResponseDto(allReady = allReady)
    }

    @Transactional(readOnly = true)
    fun getRoomStatus(code: String, playerId: String): RoomStatusDto {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException("Room not found: $code")
        val players = playerRepository.findByRoomId(room.id!!)
        val myPlayer = players.firstOrNull { it.id == playerId }
            ?: throw InvalidPlayerException("Player not in room: $playerId")

        var currentQuestion: QuestionDto? = null
        var myAnswered = false

        if (room.status == RoomStatus.IN_PROGRESS && room.currentSeq > 0) {
            val rq = roomQuestionRepository.findByRoomIdAndSeq(room.id!!, room.currentSeq)
            if (rq != null) {
                val q = questionRepository.findById(rq.questionId).orElse(null)
                if (q != null) {
                    currentQuestion = QuestionDto(room.currentSeq, q.optionA, q.optionB)
                }
            }
            myAnswered = answerRepository.existsByRoomIdAndPlayerIdAndSeq(room.id!!, playerId, room.currentSeq)
        }

        return RoomStatusDto(
            status = room.status.name,
            playerCount = players.size,
            myReady = myPlayer.isReady,
            readyCount = players.count { it.isReady },
            currentSeq = room.currentSeq,
            currentQuestion = currentQuestion,
            myAnsweredCurrentSeq = myAnswered,
        )
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
