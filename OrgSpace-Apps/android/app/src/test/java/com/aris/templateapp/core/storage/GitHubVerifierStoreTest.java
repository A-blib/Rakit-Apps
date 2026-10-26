package com.aris.templateapp.core.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GitHubVerifierStoreTest {

    /** Contoh resmi RFC 7636 (Appendix B); backend diuji dengan contoh yang sama (AuthTicketServiceTest). */
    @Test
    public void challengeMatchesThePkceStandard() {
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
                GitHubVerifierStore.challengeOf("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
    }
}
