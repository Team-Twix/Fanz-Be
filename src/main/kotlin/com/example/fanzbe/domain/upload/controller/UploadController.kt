package com.example.fanzbe.domain.upload.controller

import com.example.fanzbe.domain.upload.dto.StoredFileResponse
import com.example.fanzbe.domain.upload.service.FileStorageService
import com.example.fanzbe.global.common.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/uploads")
class UploadController(
    private val fileStorageService: FileStorageService,
) {

    @PostMapping("/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<ApiResponse<StoredFileResponse>> =
        created(fileStorageService.storeImage(file))

    @PostMapping("/files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<ApiResponse<StoredFileResponse>> =
        created(fileStorageService.storeFile(file))

    private fun created(response: StoredFileResponse): ResponseEntity<ApiResponse<StoredFileResponse>> =
        ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))
}
