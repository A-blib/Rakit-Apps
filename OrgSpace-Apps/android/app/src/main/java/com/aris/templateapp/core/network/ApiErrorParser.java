package com.aris.templateapp.core.network;

import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.ErrorResponseDto;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;

/**
 * Mengubah kegagalan request menjadi {@link ApiError} yang seragam:
 * - respons error dari backend (ErrorResponse JSON) → ApiError dengan code dari backend
 * - tidak ada koneksi / backend tidak terjangkau → {@code NETWORK_ERROR}
 * - selain itu → {@code UNKNOWN_ERROR}
 */
@Singleton
public class ApiErrorParser {

    private final Gson gson;

    @Inject
    public ApiErrorParser(Gson gson) {
        this.gson = gson;
    }

    /** Untuk respons HTTP yang tidak sukses (4xx/5xx). */
    public ApiError parse(retrofit2.Response<?> response) {
        return parseBody(response.errorBody());
    }

    ApiError parseBody(ResponseBody errorBody) {
        if (errorBody == null) {
            return ApiError.of(ApiError.UNKNOWN_ERROR);
        }
        try {
            ErrorResponseDto dto = gson.fromJson(errorBody.charStream(), ErrorResponseDto.class);
            if (dto == null || dto.code == null) {
                return ApiError.of(ApiError.UNKNOWN_ERROR);
            }
            return new ApiError(dto.code, dto.message, dto.fieldErrors, dto.existingMethods, dto.linkToken);
        } catch (JsonParseException e) {
            return ApiError.of(ApiError.UNKNOWN_ERROR);
        } finally {
            errorBody.close();
        }
    }

    /** Untuk exception saat request (mis. dari Call.execute()). IOException berarti masalah jaringan. */
    public ApiError parse(Throwable throwable) {
        return throwable instanceof IOException
                ? ApiError.of(ApiError.NETWORK_ERROR)
                : ApiError.of(ApiError.UNKNOWN_ERROR);
    }
}
