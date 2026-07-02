package ia.antop.ogam.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class RoomNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class RoomFullException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class AlreadyAnsweredException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidPlayerException(message: String) : RuntimeException(message)
