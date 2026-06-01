package com.autoever.grouplocationsharing.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val message: String) {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 방입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 입장한 사용자입니다."),
    NOT_JOINED(HttpStatus.BAD_REQUEST, "미참여 사용자입니다."),
    ROOM_FULL(HttpStatus.CONFLICT, "정원을 초과하였습니다."),
    LOCATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "방에 참여한 사용자만 위치를 공유할 수 있습니다."),
}
