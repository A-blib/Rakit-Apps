package com.aris.templateapp.core.network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AuthInterceptorTest {

    @Test
    public void authEndpointsDoNotGetToken() {
        assertFalse(AuthInterceptor.needsToken("/api/auth/login"));
        assertFalse(AuthInterceptor.needsToken("/api/auth/refresh"));
        assertFalse(AuthInterceptor.needsToken("/api/auth/github/exchange"));
    }

    @Test
    public void otherEndpointsGetToken() {
        assertTrue(AuthInterceptor.needsToken("/api/users/me"));
        // Mengandung kata "auth" tetapi bukan di bawah /api/auth/: tetap butuh token.
        assertTrue(AuthInterceptor.needsToken("/api/users/me/identities/github/authorize-url"));
    }
}
