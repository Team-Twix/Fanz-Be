package com.example.fanzbe.global.config

import com.example.fanzbe.domain.upload.service.FileStorageService
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.Base64
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import tools.jackson.databind.ObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UploadSecurityIntegrationTest(
    @Autowired private val environment: Environment,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val fileStorageService: FileStorageService,
) {
    private val httpClient = HttpClient.newHttpClient()

    @Test
    fun `image upload is available before signup`() {
        val response = upload("/api/uploads/images", "profile.png", "image/png", ONE_PIXEL_PNG)

        assertEquals(201, response.statusCode())
        val storedUrl = objectMapper.readTree(response.body()).path("data").path("url").stringValue()
        assertTrue(storedUrl.matches(Regex("/uploads/[0-9a-f-]+\\.png")))

        val storedFile = fileStorageService.uploadRoot().resolve(storedUrl.substringAfterLast('/'))
        try {
            assertTrue(Files.exists(storedFile))
        } finally {
            Files.deleteIfExists(storedFile)
        }
    }

    @Test
    fun `uploaded image supports public get and head requests from any origin`() {
        val uploadResponse = upload("/api/uploads/images", "profile.png", "image/png", ONE_PIXEL_PNG)
        assertEquals(201, uploadResponse.statusCode())

        val storedUrl = objectMapper.readTree(uploadResponse.body()).path("data").path("url").stringValue()
        val storedFile = fileStorageService.uploadRoot().resolve(storedUrl.substringAfterLast('/'))

        try {
            val preflightResponse = publicImagePreflight(storedUrl)
            assertEquals(200, preflightResponse.statusCode())
            assertEquals("*", preflightResponse.headers().firstValue("Access-Control-Allow-Origin").orElse(null))
            assertTrue(
                preflightResponse.headers().firstValue("Access-Control-Allow-Methods").orElse("")
                    .split(",")
                    .map(String::trim)
                    .containsAll(listOf("GET", "HEAD")),
            )

            val getResponse = publicImageRequest(storedUrl, "GET")
            assertEquals(200, getResponse.statusCode())
            assertEquals("image/png", getResponse.headers().firstValue("Content-Type").orElse(null))
            assertEquals("*", getResponse.headers().firstValue("Access-Control-Allow-Origin").orElse(null))
            assertTrue(getResponse.headers().firstValue("Access-Control-Allow-Credentials").isEmpty)
            assertContentEquals(ONE_PIXEL_PNG, getResponse.body())

            val headResponse = publicImageRequest(storedUrl, "HEAD")
            assertEquals(200, headResponse.statusCode())
            assertEquals("image/png", headResponse.headers().firstValue("Content-Type").orElse(null))
            assertEquals("*", headResponse.headers().firstValue("Access-Control-Allow-Origin").orElse(null))
            assertTrue(headResponse.headers().firstValue("Access-Control-Allow-Credentials").isEmpty)
            assertEquals(ONE_PIXEL_PNG.size.toString(), headResponse.headers().firstValue("Content-Length").orElse(null))
            assertTrue(headResponse.body().isEmpty())
        } finally {
            Files.deleteIfExists(storedFile)
        }
    }

    @Test
    fun `generic file upload still requires authentication`() {
        val response = upload("/api/uploads/files", "chat.txt", "text/plain", "hello".toByteArray())

        assertEquals(403, response.statusCode())
    }

    private fun upload(
        path: String,
        filename: String,
        contentType: String,
        bytes: ByteArray,
    ): HttpResponse<String> {
        val boundary = "FanzBoundary${UUID.randomUUID()}"
        val prefix = buildString {
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"file\"; filename=\"$filename\"\r\n")
            append("Content-Type: $contentType\r\n\r\n")
        }.toByteArray()
        val suffix = "\r\n--$boundary--\r\n".toByteArray()
        val body = prefix + bytes + suffix
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:${port()}$path"))
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun publicImageRequest(path: String, method: String): HttpResponse<ByteArray> {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:${port()}$path"))
            .header("Origin", ARBITRARY_FRONTEND_ORIGIN)
            .method(method, HttpRequest.BodyPublishers.noBody())
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
    }

    private fun publicImagePreflight(path: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:${port()}$path"))
            .header("Origin", ARBITRARY_FRONTEND_ORIGIN)
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun port(): Int =
        environment.getProperty("local.server.port")?.toInt()
            ?: error("local.server.port is unavailable")

    companion object {
        private const val ARBITRARY_FRONTEND_ORIGIN = "https://frontend.example"
        private val ONE_PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=",
        )
    }
}
