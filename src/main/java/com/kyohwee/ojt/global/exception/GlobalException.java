package com.kyohwee.ojt.global.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final GlobalErrorCode globalErrorCode;

    /** 1) 에러코드만 지정할 때 사용 */
    public GlobalException(GlobalErrorCode globalErrorCode) {
        super(globalErrorCode.getMessage());
        this.globalErrorCode = globalErrorCode;
    }

    /** 2) 에러코드 + 커스텀 메시지를 지정할 때 사용 */
    public GlobalException(GlobalErrorCode globalErrorCode, String message) {
        super(message != null ? message : globalErrorCode.getMessage());
        this.globalErrorCode = globalErrorCode;
    }
}
