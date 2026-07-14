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
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_003", "비밀번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_004", "아이디 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found."),
    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 아이디입니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "Chat room not found."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "CHAT_002", "Already joined chat room."),
    NOT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "CHAT_003", "User is not a chat room member."),
    HANDLE_DUPLICATED(HttpStatus.CONFLICT, "PROFILE_001", "이미 사용 중인 핸들입니다."),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_001", "자기 자신은 팔로우할 수 없습니다."),
    DM_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_001", "DM 대화방을 찾을 수 없습니다."),
    NOT_DM_PARTICIPANT(HttpStatus.FORBIDDEN, "DM_002", "DM 대화 참여자가 아닙니다."),
    CANNOT_DM_SELF(HttpStatus.BAD_REQUEST, "DM_003", "자기 자신과는 DM 할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Internal server error."),
}
