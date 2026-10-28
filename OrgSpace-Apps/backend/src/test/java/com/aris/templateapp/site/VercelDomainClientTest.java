package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** VercelDomainClient dengan server Vercel palsu: tidak ada request sungguhan ke internet. */
class VercelDomainClientTest {

    private static final String DOMAINS = "https://api.vercel.com/v10/projects/prj_hosting/domains?teamId=team_os";
    private static final String DOMAIN = "https://api.vercel.com/v9/projects/prj_hosting/domains/osis.id?teamId=team_os";

    private MockRestServiceServer vercel;
    private VercelDomainClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        vercel = MockRestServiceServer.bindTo(builder).build();
        client = new VercelDomainClient("tok_rahasia", "prj_hosting", "team_os", builder);
    }

    @Test
    void addsDomainWithBearerToken() {
        vercel.expect(requestTo(DOMAINS)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer tok_rahasia"))
                .andExpect(jsonPath("$.name").value("osis.id"))
                .andRespond(withSuccess("{\"name\":\"osis.id\",\"verified\":true}", MediaType.APPLICATION_JSON));

        assertThat(client.addDomain("osis.id")).isNull();
        vercel.verify();
    }

    @Test
    void unverifiedDomainReturnsTheExtraTxtRecord() {
        vercel.expect(requestTo(DOMAINS)).andRespond(withSuccess("""
                {"name":"osis.id","verified":false,
                 "verification":[{"type":"TXT","domain":"_vercel.osis.id","value":"vc-domain-verify=osis.id,abc"}]}""",
                MediaType.APPLICATION_JSON));

        assertThat(client.addDomain("osis.id")).contains("_vercel.osis.id").contains("vc-domain-verify=osis.id,abc");
    }

    @Test
    void alreadyInThisProjectIsFine() {
        vercel.expect(requestTo(DOMAINS)).andRespond(withStatus(HttpStatus.CONFLICT));
        vercel.expect(requestTo(DOMAIN)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"name\":\"osis.id\",\"verified\":true}", MediaType.APPLICATION_JSON));

        assertThat(client.addDomain("osis.id")).isNull();
    }

    @Test
    void domainOwnedByAnotherVercelAccountIsRejected() {
        vercel.expect(requestTo(DOMAINS)).andRespond(withStatus(HttpStatus.CONFLICT));
        vercel.expect(requestTo(DOMAIN)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.addDomain("osis.id"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("akun Vercel lain");
    }

    @Test
    void serverErrorFailsTheVerification() {
        vercel.expect(requestTo(DOMAINS)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.addDomain("osis.id")).isInstanceOf(ApiException.class);
    }

    @Test
    void removingAMissingDomainIsFine() {
        vercel.expect(requestTo(DOMAIN)).andExpect(method(HttpMethod.DELETE)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        client.removeDomain("osis.id");
        vercel.verify();
    }

    @Test
    void withoutTokenNothingIsSent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer none = MockRestServiceServer.bindTo(builder).build();
        VercelDomainClient disabled = new VercelDomainClient("", "", "", builder);

        assertThat(disabled.enabled()).isFalse();
        assertThat(disabled.addDomain("osis.id")).isNull();
        disabled.removeDomain("osis.id");
        none.verify();
    }
}
