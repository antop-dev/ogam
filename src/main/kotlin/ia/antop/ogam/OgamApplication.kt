package ia.antop.ogam

import ia.antop.ogam.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
