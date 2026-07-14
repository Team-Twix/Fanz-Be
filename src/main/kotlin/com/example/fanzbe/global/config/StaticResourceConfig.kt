package com.example.fanzbe.global.config

import com.example.fanzbe.domain.upload.service.FileStorageService
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class StaticResourceConfig(
    private val fileStorageService: FileStorageService,
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val resourceLocation = fileStorageService.uploadRoot().toUri().toString().trimEnd('/') + "/"
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(resourceLocation)
    }
}
