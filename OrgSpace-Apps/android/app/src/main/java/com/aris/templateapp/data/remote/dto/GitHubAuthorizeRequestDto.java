package com.aris.templateapp.data.remote.dto;

/** Body /auth/github/authorize-url. */
public class GitHubAuthorizeRequestDto {
    public final String linkToken;

    public GitHubAuthorizeRequestDto(String linkToken) {
        this.linkToken = linkToken;
    }
}
