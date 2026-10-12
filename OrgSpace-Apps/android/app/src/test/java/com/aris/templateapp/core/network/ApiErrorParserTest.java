package com.aris.templateapp.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ApiError;
import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

/** Unit test biasa (JVM laptop, tanpa HP): cukup cepat untuk dijalankan setiap saat. */
public class ApiErrorParserTest {

    private final ApiErrorParser parser = new ApiErrorParser(new Gson());

    @Test
    public void parsesBackendErrorWithFieldErrors() {
        ApiError error = parser.parseBody(json("""
                {"code":"VALIDATION_ERROR","message":"Data tidak valid.","fieldErrors":{"email":"Format email tidak valid"}}"""));

        assertEquals("VALIDATION_ERROR", error.getCode());
        assertEquals("Data tidak valid.", error.getMessage());
        assertEquals("Format email tidak valid", error.getFieldErrors().get("email"));
    }

    @Test
    public void parsesAccountLinkRequired() {
        ApiError error = parser.parseBody(json("""
                {"code":"ACCOUNT_LINK_REQUIRED","message":"...","existingMethods":["google"],"linkToken":"abc"}"""));

        assertEquals("ACCOUNT_LINK_REQUIRED", error.getCode());
        assertEquals("google", error.getExistingMethods().get(0));
        assertEquals("abc", error.getLinkToken());
    }

    @Test
    public void nonJsonBodyBecomesUnknownError() {
        ApiError error = parser.parseBody(ResponseBody.create("<html>502 Bad Gateway</html>", MediaType.get("text/html")));

        assertEquals(ApiError.UNKNOWN_ERROR, error.getCode());
        assertTrue(error.getFieldErrors().isEmpty());
    }

    @Test
    public void ioExceptionBecomesNetworkError() {
        ApiError error = parser.parse(new IOException("Failed to connect to localhost/127.0.0.1:8080"));

        assertTrue(error.isNetworkError());
    }

    @Test
    public void otherExceptionBecomesUnknownError() {
        assertEquals(ApiError.UNKNOWN_ERROR, parser.parse(new IllegalStateException()).getCode());
    }

    private static ResponseBody json(String body) {
        return ResponseBody.create(body, MediaType.get("application/json"));
    }
}
