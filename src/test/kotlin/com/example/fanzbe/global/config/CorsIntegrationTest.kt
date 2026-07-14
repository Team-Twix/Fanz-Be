package com.example.fanzbe.global.config

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CorsIntegrationTest(
    @Autowired private val environment: Environment,
) {
    private val httpClient = HttpClient.newHttpClient()

    @Test
    fun `localhost 5173 origins can call the API`() {
        listOf("http://localhost:5173", "http://127.0.0.1:5173").forEach { origin ->
            val response = preflight(origin)

            assertEquals(200, response.statusCode())
            assertEquals(origin, response.headers().firstValue("Access-Control-Allow-Origin").orElse(null))
            assertEquals("true", response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null))
            assertTrue(
                response.headers().firstValue("Access-Control-Allow-Methods").orElse("")
                    .split(",")
                    .map(String::trim)
                    .contains("GET"),
            )
        }
    }

    @Test
    fun `unknown origins are rejected`() {
        val response = preflight("https://untrusted.example")

        assertEquals(403, response.statusCode())
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty)
    }

    private fun preflight(origin: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(
            URI.create("http://127.0.0.1:${port()}/api/auth/username-availability?username=cors-test"),
        )
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .header("Origin", origin)
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization,content-type")
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun port(): Int =
        environment.getProperty("local.server.port")?.toInt()
            ?: error("local.server.port is unavailable")
}
