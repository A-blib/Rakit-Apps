package com.aris.templateapp.common.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Error yang memang direncanakan (bukan bug). Lempar dari service, lalu
 * {@link GlobalExceptionHandler} mengubahnya menjadi {@code ErrorResponse} dengan status yang sesuai.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<String> existingMethods;
    private final String linkToken;
    private final Map<String, String> fieldErrors;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public ApiException(ErrorCode errorCode, String message, List<String> existingMethods, String linkToken) {
        super(message);
        this.errorCode = errorCode;
        this.existingMethods = existingMethods;
        this.linkToken = linkToken;
        this.fieldErrors = null;
    }

    /** Validasi yang hanya bisa dicek service (bergantung pada isian lain): pesan ditempel ke field form-nya. */
    public ApiException(ErrorCode errorCode, String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = errorCode;
        this.existingMethods = null;
        this.linkToken = null;
        this.fieldErrors = fieldErrors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.status();
    }

    public List<String> getExistingMethods() {
        return existingMethods;
    }

    public String getLinkToken() {
        return linkToken;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
