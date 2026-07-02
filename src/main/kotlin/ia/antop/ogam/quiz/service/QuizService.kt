package ia.antop.ogam.quiz.service

import ia.antop.ogam.common.exception.AlreadyAnsweredException
import ia.antop.ogam.common.exception.InvalidPlayerException
import ia.antop.ogam.common.exception.RoomNotFoundException
import ia.antop.ogam.quiz.dto.QuestionResultDto
import ia.antop.ogam.quiz.dto.ResultDto
import ia.antop.ogam.quiz.dto.SubmitAnswerResponseDto
import ia.antop.ogam.quiz.entity.Answer
import ia.antop.ogam.quiz.entity.RoomStatus
import ia.antop.ogam.quiz.event.OpponentAnsweredEvent
import ia.antop.ogam.quiz.event.QuestionAdvancedEvent
import ia.antop.ogam.quiz.event.QuizCompletedEvent
import ia.antop.ogam.quiz.repository.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuizService(
    private val roomRepository: RoomRepository,
    private val playerRepository: PlayerRepository,
    private val roomQuestionRepository: RoomQuestionRepository,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun submitAnswer(code: String, playerId: String, seq: Int, choice: String): SubmitAnswerResponseDto {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException("Room not found: $code")

        if (room.status != RoomStatus.IN_PROGRESS) {
            throw IllegalStateException("Quiz is not in progress")
        }

        playerRepository.findByIdAndRoomId(playerId, room.id!!)
            ?: throw InvalidPlayerException("Player not in room: $playerId")

        if (answerRepository.existsByRoomIdAndPlayerIdAndSeq(room.id!!, playerId, seq)) {
            throw AlreadyAnsweredException("Already answered seq=$seq")
        }

        answerRepository.save(Answer(roomId = room.id!!, playerId = playerId, seq = seq, choice = choice))

        val answers = answerRepository.findByRoomIdAndSeq(room.id!!, seq)
        val playerCount = playerRepository.findByRoomId(room.id!!).size

        log.info("Answer saved: code={}, playerId={}, seq={}, choice={}, count={}/{}", code, playerId, seq, choice, answers.size, playerCount)

        if (answers.size >= playerCount) {
            if (seq >= 10) {
                room.status = RoomStatus.COMPLETED
                roomRepository.save(room)
                eventPublisher.publishEvent(QuizCompletedEvent(code))
            } else {
                val nextSeq = seq + 1
                room.currentSeq = nextSeq
                roomRepository.save(room)

                val nextRq = roomQuestionRepository.findByRoomIdAndSeq(room.id!!, nextSeq)!!
                val nextQ = questionRepository.findById(nextRq.questionId).orElseThrow()
                eventPublisher.publishEvent(QuestionAdvancedEvent(code, nextSeq, nextQ.optionA, nextQ.optionB))
            }
        } else {
            eventPublisher.publishEvent(OpponentAnsweredEvent(code, seq))
        }

        return SubmitAnswerResponseDto(recorded = true)
    }

    @Transactional(readOnly = true)
    fun getResults(code: String, playerId: String): ResultDto {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException("Room not found: $code")
        val players = playerRepository.findByRoomId(room.id!!)

        players.firstOrNull { it.id == playerId }
            ?: throw InvalidPlayerException("Player not in room: $playerId")

        val opponentId = players.firstOrNull { it.id != playerId }?.id
        val allAnswers = answerRepository.findByRoomId(room.id!!)
        val roomQuestions = roomQuestionRepository.findByRoomIdOrderBySeq(room.id!!)

        val questionResults = roomQuestions.map { rq ->
            val question = questionRepository.findById(rq.questionId).orElseThrow()
            val myAnswer = allAnswers.firstOrNull { it.playerId == playerId && it.seq == rq.seq }
            val opponentAnswer = allAnswers.firstOrNull { it.playerId == opponentId && it.seq == rq.seq }

            QuestionResultDto(
                seq = rq.seq,
                optionA = question.optionA,
                optionB = question.optionB,
                myChoice = myAnswer?.choice,
                opponentChoice = opponentAnswer?.choice,
                matched = myAnswer?.choice != null && myAnswer.choice == opponentAnswer?.choice,
            )
        }

        val matchRate = questionResults.count { it.matched } * 10
        return ResultDto(matchRate = matchRate, questions = questionResults)
    }
}
