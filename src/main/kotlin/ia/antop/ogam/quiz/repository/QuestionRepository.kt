package ia.antop.ogam.quiz.repository

import ia.antop.ogam.quiz.entity.Question
import org.springframework.data.jpa.repository.JpaRepository

interface QuestionRepository : JpaRepository<Question, Long>
