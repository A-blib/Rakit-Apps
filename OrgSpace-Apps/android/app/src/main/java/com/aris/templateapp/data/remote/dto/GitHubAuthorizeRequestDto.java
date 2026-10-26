package com.aris.templateapp.data.remote.dto;

/** Body /auth/github/authorize-url dan /users/me/identities/github/authorize-url. */
public class GitHubAuthorizeRequestDto {
    public final String linkToken;
    /** Hash verifier dari {@code GitHubVerifierStore}; verifier-nya sendiri tetap di HP. */
    public final String codeChallenge;

    public GitHubAuthorizeRequestDto(String linkToken, String codeChallenge) {
        this.linkToken = linkToken;
        this.codeChallenge = codeChallenge;
    }
}
