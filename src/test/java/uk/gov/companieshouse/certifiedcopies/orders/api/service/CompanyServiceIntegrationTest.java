package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.ApiErrorResponsePayload;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.Error;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestUtils.givenSdkIsConfigured;

/**
 * Unit/integration tests the {@link CompanyService} class.
 */
@SpringBootTest
@AutoConfigureWireMock(port = 0)
public class CompanyServiceIntegrationTest {

    @ClassRule
    public static final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

    private static final String COMPANY_NUMBER = "00006400";
    private static final String EXPECTED_COMPANY_NAME = "THE GIRLS' DAY SCHOOL TRUST";

    private static final CompanyProfileApi COMPANY_PROFILE;

    private static final ApiErrorResponsePayload COMPANY_NOT_FOUND =
            new ApiErrorResponsePayload(singletonList(new Error("ch:service", "company-profile-not-found")));

    static {
        COMPANY_PROFILE = new CompanyProfileApi();
        COMPANY_PROFILE.setCompanyName(EXPECTED_COMPANY_NAME);
    }

    @Autowired
    private CompanyService serviceUnderTest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;


    @Test
    void getCompanyNameGetsNameSuccessfully () throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/company/" + COMPANY_NUMBER))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(COMPANY_PROFILE))));

        // When and then
        assertThat(serviceUnderTest.getCompanyName(COMPANY_NUMBER), is(EXPECTED_COMPANY_NAME));
    }

    @Test
    void getCompanyNameThrowsBadRequestExceptionForCompanyNotFound () throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/company/" + COMPANY_NUMBER))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(COMPANY_NOT_FOUND))));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getCompanyName(COMPANY_NUMBER));
        assertThat(exception.getStatusCode(), is(BAD_REQUEST));
        assertThat(exception.getReason(), is("Error getting company name for company number 00006400"));
    }

    @Test
    void getCompanyNameThrowsInternalServerErrorForConnectionFailure() {

        // Given
        final String wireMockPort = givenSdkIsConfigured(environment, ENVIRONMENT_VARIABLES);
        givenThat(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/company/" + COMPANY_NUMBER))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getCompanyName(COMPANY_NUMBER));
        assertThat(exception.getStatusCode(), is(INTERNAL_SERVER_ERROR));
        final String expectedReason = "Error sending request to http://localhost:"
                + wireMockPort + "/company/" + COMPANY_NUMBER + ": Connection reset";
        assertThat(exception.getReason(), is(expectedReason));
    }

}

