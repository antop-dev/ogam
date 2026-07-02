package ia.antop.ogam.quiz.controller

import ia.antop.ogam.config.AppProperties
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class PageController(
    private val appProperties: AppProperties,
) {
    @GetMapping("/")
    fun home(request: HttpServletRequest, model: Model): String {
        model.addAttribute("contextPath", request.contextPath)
        return "index"
    }

    @GetMapping("/room/{code}")
    fun room(
        @PathVariable code: String,
        request: HttpServletRequest,
        model: Model,
    ): String {
        model.addAttribute("roomCode", code)
        model.addAttribute("baseUrl", appProperties.baseUrl)
        model.addAttribute("contextPath", request.contextPath)
        return "room"
    }
}
