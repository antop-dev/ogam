package ia.antop.ogam.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val dbPath: String = "ogam.db",
    val baseUrl: String = "http://localhost:8080",
)
