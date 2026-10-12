package com.aris.templateapp.common.exception;

import com.aris.templateapp.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mengubah semua exception dari controller menjadi {@link ErrorResponse}, sehingga app Android
 * selalu menerima bentuk error yang sama dan tidak pernah melihat stack trace.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(
                ex.getErrorCode().name(), ex.getMessage(), ex.getFieldErrors(), ex.getExistingMethods(), ex.getLinkToken(),
                ex.getDetails()));
    }

    /** Gagal validasi {@code @Valid} pada body request: kirim pesan per field agar form bisa menandai input yang salah. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Satu field bisa melanggar beberapa aturan; cukup tampilkan yang pertama.
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), fieldErrors);
    }

    /** Body kosong atau JSON rusak. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(ErrorCode.VALIDATION_ERROR, "Format request tidak valid.", null);
    }

    /** File multipart (mis. logo organisasi) melebihi batas Spring sebelum sempat diperiksa service. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException ex) {
        return build(ErrorCode.UPLOAD_TOO_LARGE, "Ukuran file terlalu besar.", null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        return build(ErrorCode.VALIDATION_ERROR, "File belum dipilih.", null);
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return build(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.defaultMessage(), null);
    }

    /** Error tak terduga (bug). Detailnya hanya ditulis ke log server, bukan ke response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Error tak terduga", ex);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.defaultMessage(), null);
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, String message, Map<String, String> fieldErrors) {
        return ResponseEntity.status(code.status()).body(new ErrorResponse(code.name(), message, fieldErrors, null, null, null));
    }
}
