package com.aris.templateapp.ui.auth;

import static org.junit.Assert.assertEquals;

import com.aris.templateapp.data.model.LoginMethod;

import org.junit.Test;

import java.util.List;

public class LinkRequestTest {

    @Test
    public void parsesExistingMethodsAndIgnoresUnknownOnes() {
        LinkRequest request = new LinkRequest("tok", List.of("google", "local", "facebook"), LoginMethod.GITHUB);

        assertEquals(List.of(LoginMethod.GOOGLE, LoginMethod.LOCAL), request.getExistingMethods());
        assertEquals(LoginMethod.GITHUB, request.getNewMethod());
        assertEquals("tok", request.getLinkToken());
    }
}
