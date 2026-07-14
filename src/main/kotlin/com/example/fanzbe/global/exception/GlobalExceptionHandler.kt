package com.example.fanzbe.global.exception

import com.example.fanzbe.global.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = exception.errorCode
        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.failure(errorCode.code, exception.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        val fieldErrors = exception.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: ErrorCode.INVALID_INPUT.message)
        }

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.status)
            .body(
                ApiResponse.failure(
                    ErrorCode.INVALID_INPUT.code,
                    ErrorCode.INVALID_INPUT.message,
                    fieldErrors,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception", exception)

        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.status)
            .body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR.code, ErrorCode.INTERNAL_ERROR.message))
    }
}
