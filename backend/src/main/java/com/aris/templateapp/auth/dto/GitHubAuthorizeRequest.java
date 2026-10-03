package com.aris.templateapp.auth.dto;

/** @param linkToken opsional, sama seperti di {@link LoginRequest} */
public record GitHubAuthorizeRequest(String linkToken) {
}
