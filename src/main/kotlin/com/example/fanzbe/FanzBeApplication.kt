package com.example.fanzbe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class FanzBeApplication

fun main(args: Array<String>) {
    runApplication<FanzBeApplication>(*args)
}
