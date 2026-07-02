package ia.antop.ogam.quiz.service

import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service
class SseEmitterService(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>>()

    fun subscribe(roomCode: String): SseEmitter {
        val emitter = SseEmitter(36_000_000L)
        val roomEmitters = emitters.getOrPut(roomCode) { CopyOnWriteArrayList() }
        roomEmitters.add(emitter)

        emitter.onCompletion { roomEmitters.remove(emitter) }
        emitter.onTimeout { roomEmitters.remove(emitter) }
        emitter.onError { roomEmitters.remove(emitter) }

        try {
            val payload = objectMapper.writeValueAsString(mapOf("type" to "CONNECTED"))
            emitter.send(SseEmitter.event().name("CONNECTED").data(payload))
        } catch (e: Exception) {
            log.warn("Failed to send initial SSE event: {}", e.message)
        }

        return emitter
    }

    fun emit(roomCode: String, type: String, data: Any = emptyMap<String, Any>()) {
        val payload = objectMapper.writeValueAsString(mapOf("type" to type, "data" to data))
        val roomEmitters = emitters[roomCode] ?: return

        roomEmitters.forEach { emitter ->
            try {
                emitter.send(SseEmitter.event().name(type).data(payload))
            } catch (e: Exception) {
                log.warn("SSE emit failed for room={}, type={}: {}", roomCode, type, e.message)
                roomEmitters.remove(emitter)
            }
        }
    }
}
