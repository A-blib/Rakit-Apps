package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Validasi link isian (alur-buat-website-via-template.md bagian 6.4). */
public class LinkRulesTest {

    @Test
    public void allowedSchemes() {
        assertTrue(LinkRules.isValid("https://tokokue.id"));
        assertTrue(LinkRules.isValid("http://contoh.com/menu?x=1"));
        assertTrue(LinkRules.isValid("mailto:halo@tokokue.id"));
        assertTrue(LinkRules.isValid("tel:+62221234567"));
        assertTrue(LinkRules.isValid("https://wa.me/6281234567890"));
    }

    @Test
    public void rejectedLinks() {
        assertFalse(LinkRules.isValid("javascript:alert(1)"));
        assertFalse(LinkRules.isValid("JavaScript:alert(1)"));
        assertFalse(LinkRules.isValid("data:text/html,hai"));
        assertFalse(LinkRules.isValid("tokokue.id"));
        assertFalse(LinkRules.isValid("https://"));
        assertFalse(LinkRules.isValid("ftp://contoh.com"));
        assertFalse(LinkRules.isValid(null));
    }

    @Test
    public void whatsappHelper() {
        assertEquals("https://wa.me/6281234567890", LinkRules.whatsappLink("0812-3456-7890"));
        assertEquals("https://wa.me/6281234567890", LinkRules.whatsappLink("+62 812 3456 7890"));
        assertEquals("https://wa.me/6281234567890", LinkRules.whatsappLink("81234567890"));
        assertNull(LinkRules.whatsappLink("123"));
        assertEquals("081234567890", LinkRules.whatsappNumber("https://wa.me/6281234567890"));
    }

    @Test
    public void externalSampleLinks() {
        assertTrue(LinkRules.isExternal("https://wa.me/62812"));
        assertFalse(LinkRules.isExternal("kontak.html"));
        assertFalse(LinkRules.isExternal("#menu"));
    }
}
