package com.kyohwee.ojt.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode {
    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 에러, 관리자에게 문의 바립니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "요청 형식이 잘못되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증 되지 않은 요청입니다."),

    // 유저 관련
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "LOGIN_401", "아이디 또는 비밀번호가 잘못되었습니다."),
    USER_EXIST(HttpStatus.CONFLICT, "USER_409", "중복된 유저가 존재합니다."), // 추가된 에러 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."), // 추가된 에러 코드

    BATCH_JOB_NOT_FOUND(    HttpStatus.NOT_FOUND,        "BATCH_404",   "해당 배치 작업을 찾을 수 없습니다."),        // :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
    SCHEDULE_NOT_FOUND(     HttpStatus.NOT_FOUND,        "SCHED_404",   "해당 스케줄을 찾을 수 없습니다."),            // :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}
    SCHEDULER_REGISTRATION_ERROR( HttpStatus.INTERNAL_SERVER_ERROR, "SCHED_500", "스케줄 등록 중 오류가 발생했습니다."), // :contentReference[oaicite:4]{index=4}&#8203;:contentReference[oaicite:5]{index=5}

    ;
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}