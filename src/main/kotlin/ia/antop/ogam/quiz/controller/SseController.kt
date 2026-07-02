package ia.antop.ogam.quiz.controller

import ia.antop.ogam.quiz.service.SseEmitterService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/sse")
class SseController(
    private val sseEmitterService: SseEmitterService,
) {

    @GetMapping(
        value = ["/rooms/{code}"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun subscribe(
        @PathVariable code: String,
    ): SseEmitter = sseEmitterService.subscribe(code)
}
