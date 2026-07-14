package com.example.fanzbe.domain.upload.service

import com.example.fanzbe.domain.upload.config.UploadProperties
import com.example.fanzbe.domain.upload.dto.StoredFileResponse
import com.example.fanzbe.global.exception.BusinessException
import com.example.fanzbe.global.exception.ErrorCode
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileStorageService(
    private val properties: UploadProperties,
) {

    fun storeImage(file: MultipartFile): StoredFileResponse =
        store(file, UploadKind.IMAGE)

    fun storeFile(file: MultipartFile): StoredFileResponse =
        store(file, UploadKind.FILE)

    fun uploadRoot(): Path = Path.of(properties.directory).toAbsolutePath().normalize()

    private fun store(file: MultipartFile, kind: UploadKind): StoredFileResponse {
        if (file.isEmpty || file.size <= 0) {
            throw BusinessException(ErrorCode.UPLOAD_EMPTY)
        }

        val contentType = file.contentType?.lowercase()
            ?: throw BusinessException(ErrorCode.UPLOAD_TYPE_NOT_ALLOWED)
        if (!kind.accepts(contentType)) {
            throw BusinessException(ErrorCode.UPLOAD_TYPE_NOT_ALLOWED)
        }

        val maxBytes = if (kind == UploadKind.IMAGE) properties.maxImageBytes else properties.maxFileBytes
        if (file.size > maxBytes) {
            throw BusinessException(ErrorCode.UPLOAD_TOO_LARGE)
        }

        val originalFilename = sanitizeOriginalFilename(file.originalFilename)
        val storedFilename = UUID.randomUUID().toString() + requireNotNull(CONTENT_TYPE_EXTENSIONS[contentType])
        val root = uploadRoot()
        val target = root.resolve(storedFilename).normalize()
        if (!target.startsWith(root)) {
            throw BusinessException(ErrorCode.UPLOAD_FAILED)
        }

        try {
            Files.createDirectories(root)
            file.inputStream.use { input ->
                Files.copy(input, target)
            }
        } catch (exception: IOException) {
            runCatching { Files.deleteIfExists(target) }
            throw BusinessException(ErrorCode.UPLOAD_FAILED, cause = exception)
        }

        return StoredFileResponse(
            url = "/uploads/$storedFilename",
            originalFilename = originalFilename,
            contentType = contentType,
            size = file.size,
        )
    }

    private fun sanitizeOriginalFilename(filename: String?): String {
        val sanitized = filename
            ?.replace('\\', '/')
            ?.substringAfterLast('/')
            ?.replace(CONTROL_CHARACTERS, "")
            ?.trim()
            ?.take(MAX_ORIGINAL_FILENAME_LENGTH)
            .orEmpty()
        return sanitized.ifEmpty { "file" }
    }

    private enum class UploadKind {
        IMAGE,
        FILE;

        fun accepts(contentType: String): Boolean = when (this) {
            IMAGE -> contentType in IMAGE_CONTENT_TYPES
            FILE -> contentType in FILE_CONTENT_TYPES || contentType in IMAGE_CONTENT_TYPES
        }
    }

    companion object {
        private const val MAX_ORIGINAL_FILENAME_LENGTH = 255
        private val CONTROL_CHARACTERS = Regex("[\\u0000-\\u001F\\u007F]")
        private val IMAGE_CONTENT_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
        )
        private val FILE_CONTENT_TYPES = setOf(
            "application/pdf",
            "application/zip",
            "application/x-zip-compressed",
            "text/plain",
            "audio/mpeg",
            "video/mp4",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        )
        private val CONTENT_TYPE_EXTENSIONS = mapOf(
            "image/jpeg" to ".jpg",
            "image/png" to ".png",
            "image/gif" to ".gif",
            "image/webp" to ".webp",
            "application/pdf" to ".pdf",
            "application/zip" to ".zip",
            "application/x-zip-compressed" to ".zip",
            "text/plain" to ".txt",
            "audio/mpeg" to ".mp3",
            "video/mp4" to ".mp4",
            "application/msword" to ".doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to ".docx",
            "application/vnd.ms-excel" to ".xls",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to ".xlsx",
            "application/vnd.ms-powerpoint" to ".ppt",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" to ".pptx",
        )
    }
}
