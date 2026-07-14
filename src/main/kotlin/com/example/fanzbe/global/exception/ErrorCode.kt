package com.example.fanzbe.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "Invalid input."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Unauthorized."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Invalid token."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "Chat room not found."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "CHAT_002", "Already joined chat room."),
    NOT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "CHAT_003", "User is not a chat room member."),
    HANDLE_DUPLICATED(HttpStatus.CONFLICT, "PROFILE_001", "이미 사용 중인 핸들입니다."),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_001", "자기 자신은 팔로우할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Internal server error."),
}
