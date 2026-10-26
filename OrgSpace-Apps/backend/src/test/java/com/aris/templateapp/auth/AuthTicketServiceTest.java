package com.aris.templateapp.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTicketServiceTest {

    /** Contoh resmi RFC 7636 (Appendix B); app Android diuji dengan contoh yang sama (GitHubVerifierStoreTest). */
    @Test
    void challengeMatchesThePkceStandard() {
        assertThat(AuthTicketService.challengeOf("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
                .isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }
}
