package com.example.fanzbe.domain.upload.dto

data class StoredFileResponse(
    val url: String,
    val originalFilename: String,
    val contentType: String,
    val size: Long,
)
