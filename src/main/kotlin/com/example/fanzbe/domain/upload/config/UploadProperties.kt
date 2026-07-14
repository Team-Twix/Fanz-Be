package com.example.fanzbe.domain.upload.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.upload")
class UploadProperties {
    var directory: String = "uploads"
    var maxImageBytes: Long = 10 * 1024 * 1024
    var maxFileBytes: Long = 20 * 1024 * 1024
}
