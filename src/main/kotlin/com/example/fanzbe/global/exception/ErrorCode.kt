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
    NOT_ROOM_HOST(HttpStatus.FORBIDDEN, "CHAT_004", "방장만 가능한 작업입니다."),
    CANNOT_LEAVE_AS_HOST(HttpStatus.BAD_REQUEST, "CHAT_005", "방장은 나갈 수 없습니다. 채팅방을 삭제하세요."),
    CANNOT_KICK_HOST(HttpStatus.BAD_REQUEST, "CHAT_006", "방장은 추방할 수 없습니다."),
    CHAT_JOIN_CONDITION_NOT_MET(HttpStatus.FORBIDDEN, "CHAT_007", "채팅방 가입 조건을 충족하지 않습니다."),
    INVALID_CHAT_MESSAGE(HttpStatus.BAD_REQUEST, "CHAT_008", "메시지 내용 또는 첨부 파일이 필요합니다."),
    HANDLE_DUPLICATED(HttpStatus.CONFLICT, "PROFILE_001", "이미 사용 중인 핸들입니다."),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_001", "자기 자신은 팔로우할 수 없습니다."),
    CANNOT_RATE_SELF(HttpStatus.BAD_REQUEST, "RATING_001", "자기 자신은 평가할 수 없습니다."),
    RATING_NOT_ELIGIBLE(HttpStatus.FORBIDDEN, "RATING_002", "같은 채팅방에 참여 중인 유저만 평가할 수 있습니다."),
    UPLOAD_EMPTY(HttpStatus.BAD_REQUEST, "UPLOAD_001", "업로드할 파일이 비어 있습니다."),
    UPLOAD_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "UPLOAD_002", "허용되지 않는 파일 형식입니다."),
    UPLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD_003", "파일 크기 제한을 초과했습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_004", "파일 저장에 실패했습니다."),
    DM_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_001", "DM room not found."),
    NOT_DM_MEMBER(HttpStatus.FORBIDDEN, "DM_002", "User is not a DM room member."),
    CANNOT_DM_SELF(HttpStatus.BAD_REQUEST, "DM_003", "자기 자신과 DM을 시작할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Internal server error."),
}
