package com.aris.templateapp.organization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SocialLinksTest {

    @Test
    void instagramAcceptsLinksAndHandles() {
        assertThat(SocialLinks.instagram("https://www.instagram.com/osis.smansa/?hl=id")).isEqualTo("osis.smansa");
        assertThat(SocialLinks.instagram("@osis.smansa")).isEqualTo("osis.smansa");
        assertThat(SocialLinks.instagram("instagram.com/osis_1")).isEqualTo("osis_1");
        assertThat(SocialLinks.instagram("https://evil.com/x")).isNull();
        assertThat(SocialLinks.instagram("bukan username!")).isNull();
    }

    @Test
    void tiktokAndYoutube() {
        assertThat(SocialLinks.tiktok("https://www.tiktok.com/@osis.smansa")).isEqualTo("osis.smansa");
        assertThat(SocialLinks.youtube("https://youtube.com/@bemunu")).isEqualTo("@bemunu");
        assertThat(SocialLinks.youtube("bemunu")).isEqualTo("@bemunu");
        assertThat(SocialLinks.youtube("https://www.youtube.com/channel/UC123abc")).isEqualTo("channel/UC123abc");
    }

    @Test
    void whatsappBecomesInternationalDigits() {
        assertThat(SocialLinks.whatsapp("0812-3456-7890")).isEqualTo("6281234567890");
        assertThat(SocialLinks.whatsapp("+62 812 3456 7890")).isEqualTo("6281234567890");
        assertThat(SocialLinks.whatsapp("812345678")).isEqualTo("62812345678");
        assertThat(SocialLinks.whatsapp("123")).isNull();
    }

    @Test
    void invitationCodesAreNormalizedAndReadable() {
        String code = new InvitationCodes().newCode();
        assertThat(code).matches("[2-9A-HJ-NP-Z]{4}-[2-9A-HJ-NP-Z]{4}");
        assertThat(InvitationCodes.normalize(" osis 7k2p ")).isEqualTo("OSIS7K2P");
        assertThat(InvitationCodes.normalize("OSIS-7K2P")).isEqualTo("OSIS7K2P");
    }
}
