package com.aris.templateapp.ui.organization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.aris.templateapp.R;

import org.junit.Test;

public class PortalCredentialRulesTest {

    @Test
    public void usernameFollowsBackendRule() {
        assertNull(PortalCredentialRules.usernameError("admin.osis"));
        assertNull(PortalCredentialRules.usernameError("  Ketua_OSIS-1 "));
        assertEquals(Integer.valueOf(R.string.portal_error_username), PortalCredentialRules.usernameError("ab"));
        assertEquals(Integer.valueOf(R.string.portal_error_username), PortalCredentialRules.usernameError("ketua osis"));
        assertEquals(Integer.valueOf(R.string.portal_error_username), PortalCredentialRules.usernameError("a".repeat(41)));
    }

    @Test
    public void passwordIsEightToSeventyTwoCharacters() {
        assertNull(PortalCredentialRules.passwordError("rahasia1"));
        assertEquals(Integer.valueOf(R.string.portal_error_password), PortalCredentialRules.passwordError("pendek"));
        assertEquals(Integer.valueOf(R.string.portal_error_password), PortalCredentialRules.passwordError("p".repeat(73)));
    }
}
