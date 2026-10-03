package com.aris.templateapp.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Menguji bentuk ErrorResponse tanpa menyalakan seluruh app: cukup satu controller palsu
 * yang sengaja melempar error, ditambah GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void apiExceptionUsesItsCodeAndStatus() throws Exception {
        mockMvc.perform(get("/fake/email-used"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"))
                .andExpect(jsonPath("$.message").value("Email sudah terdaftar."))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void invalidBodyReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/fake/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bukan-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Format email tidak valid"));
    }

    @Test
    void brokenJsonReturnsValidationError() throws Exception {
        mockMvc.perform(post("/fake/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{rusak"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unexpectedErrorHidesDetails() throws Exception {
        mockMvc.perform(get("/fake/bug"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan di server."));
    }

    record FakeRequest(@NotBlank @Email(message = "Format email tidak valid") String email) {
    }

    @RestController
    static class FakeController {

        @GetMapping("/fake/email-used")
        void emailUsed() {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_USED);
        }

        @PostMapping("/fake/register")
        void register(@Valid @RequestBody FakeRequest request) {
        }

        @GetMapping("/fake/bug")
        void bug() {
            throw new IllegalStateException("rahasia internal");
        }
    }
}
