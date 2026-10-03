package com.aris.templateapp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Error yang memang direncanakan (bukan bug). Lempar dari service, lalu
 * {@link GlobalExceptionHandler} mengubahnya menjadi {@code ErrorResponse} dengan status yang sesuai.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.status();
    }
}
