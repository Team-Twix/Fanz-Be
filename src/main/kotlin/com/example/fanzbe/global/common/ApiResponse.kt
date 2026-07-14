package com.example.fanzbe.global.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
) {
    companion object {
        fun success(): ApiResponse<Nothing> =
            ApiResponse(success = true)

        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(success = true, data = data)

        fun <T> success(data: T, message: String): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        fun failure(errorCode: String, message: String): ApiResponse<Nothing> =
            ApiResponse(success = false, message = message, errorCode = errorCode)

        fun <T> failure(errorCode: String, message: String, data: T): ApiResponse<T> =
            ApiResponse(success = false, data = data, message = message, errorCode = errorCode)
    }
}
