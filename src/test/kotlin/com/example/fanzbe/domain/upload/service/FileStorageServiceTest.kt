package com.example.fanzbe.domain.upload.service

import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class FileStorageServiceTest(
    @Autowired private val fileStorageService: FileStorageService,
) {

    @Test
    fun `이미지를 UUID 파일명으로 저장한다`() {
        val file = MockMultipartFile(
            "file",
            "../avatar.png",
            "image/png",
            byteArrayOf(1, 2, 3),
        )

        val response = fileStorageService.storeImage(file)
        val stored = fileStorageService.uploadRoot().resolve(response.url.substringAfterLast('/'))
        try {
            assertEquals("avatar.png", response.originalFilename)
            assertTrue(response.url.startsWith("/uploads/"))
            assertTrue(Files.exists(stored))
        } finally {
            Files.deleteIfExists(stored)
        }
    }

    @Test
    fun `허용되지 않은 파일 형식을 거부한다`() {
        val file = MockMultipartFile("file", "run.exe", "application/x-msdownload", byteArrayOf(1))

        val exception = assertFailsWith<BusinessException> {
            fileStorageService.storeFile(file)
        }

        assertEquals(ErrorCode.UPLOAD_TYPE_NOT_ALLOWED, exception.errorCode)
    }
}
