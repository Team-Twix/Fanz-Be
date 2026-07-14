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
        assertTrue(Files.exists(storedFile))
        Files.deleteIfExists(storedFile)
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

    private fun port(): Int =
        environment.getProperty("local.server.port")?.toInt()
            ?: error("local.server.port is unavailable")

    companion object {
        private val ONE_PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=",
        )
    }
}
