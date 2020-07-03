package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.http.Fault;
import org.hamcrest.core.Is;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// TODO GCI-1209 Remove junit-pioneer import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.FILING_NOT_FOUND;

/**
 * Integration tests the {@link FilingHistoryDocumentService}.
 */
@SpringBootTest
@SpringJUnitConfig(FilingHistoryDocumentServiceIntegrationTest.Config.class)
//@AutoConfigureWireMock(port = FilingHistoryDocumentServiceIntegrationTest.WIRE_MOCK_PORT)
@AutoConfigureWireMock(port = 0)
//@SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
//@SetEnvironmentVariable(key = "API_URL", value = "http://localhost:" +
//        FilingHistoryDocumentServiceIntegrationTest.WIRE_MOCK_PORT)
//@SetEnvironmentVariable(key = "PAYMENTS_API_URL", value = "http://localhost:" +
//        FilingHistoryDocumentServiceIntegrationTest.WIRE_MOCK_PORT)
class FilingHistoryDocumentServiceIntegrationTest {

    @ClassRule
    public static final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

//    // Junit 5 Pioneer @SetEnvironmentVariable cannot evaluate properties/environment variables
//    // such as {wire.mock.port}, hence we seem to be forced to hard wire the port value. Not ideal.
//    static final int WIRE_MOCK_PORT = 12345;

    private static final String COMPANY_NUMBER = "00006400";
    private static final String UNKNOWN_COMPANY_NUMBER = "00000000";
    private static final String ID_1 = "MDAxMTEyNzExOGFkaXF6a2N4";
    private static final String ID_2 = "MzAwOTM2MDg5OWFkaXF6a2N4";
    private static final String ID_3 = "MDE2OTkyOTEwMmFkaXF6a2N4";
    private static final String ID_4 = "MDAyNzI3NTQ4OWFkaXF6a2N4";
    private static final String UNKNOWN_ID = "000000000000000000000000";

    private static final FilingHistoryDocument FILING_1 = new FilingHistoryDocument(
            "1993-04-01",
            "memorandum-articles",
            null,
            ID_1,
            "MEM/ARTS"
    );
    private static final FilingHistoryDocument FILING_2 = new FilingHistoryDocument(
            "2010-02-12",
            "change-person-director-company-with-change-date",
            Stream.of(new Object[][] {
                    { "change_date", "2010-02-12" },
                    { "officer_name", "Thomas David Wheare" }
            }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1])),
            ID_2,
            "CH01"
    );
    private static final FilingHistoryDocument FILING_3 = new FilingHistoryDocument(
            "2006-10-19",
            "legacy",
            Collections.singletonMap("description", "New director appointed"),
            ID_3,
            "288a"
    );
    private static final FilingHistoryDocument FILING_4 = new FilingHistoryDocument(
            "2005-03-21",
            "accounts-with-accounts-type-group",
            Collections.singletonMap("made_up_date", "2004-08-31"),
            ID_4,
            "AA"
    );

    private static final List<FilingHistoryDocument> FILINGS_SOUGHT = asList(
            new FilingHistoryDocument(null, null, null, ID_1, null),
            new FilingHistoryDocument(null, null, null, ID_2, null),
            new FilingHistoryDocument(null, null, null, ID_3, null),
            new FilingHistoryDocument(null, null, null, ID_4, null));

    private static final FilingHistoryDocument UNKNOWN_FILING =
            new FilingHistoryDocument(null, null, null, UNKNOWN_ID, null);

    private static final List<FilingHistoryDocument> FILINGS_EXPECTED = asList(FILING_1, FILING_2, FILING_3, FILING_4);

    @Configuration
    @ComponentScan(basePackageClasses = FilingHistoryDocumentServiceIntegrationTest.class)
    static class Config {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setPropertyNamingStrategy(SNAKE_CASE)
                    .findAndRegisterModules();
        }

    }

    @Autowired
    private FilingHistoryDocumentService serviceUnderTest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @MockBean
    private CertifiedCopyItemService certifiedCopyItemService;

    @Test
    @DisplayName("getFilingHistoryDocuments gets the expected filing history documents successfully")
    void getFilingHistoryDocumentsSuccessfully() throws JsonProcessingException {

        final String wireMockPort = environment.getProperty("wiremock.server.port");

        // Given
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://localhost:" + wireMockPort);
        ENVIRONMENT_VARIABLES.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(filingApi(FILING_1)))));
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_2))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(filingApi(FILING_2)))));
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_3))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(filingApi(FILING_3)))));
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_4))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(filingApi(FILING_4)))));

        // When
        final List<FilingHistoryDocument> filings =
                serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, FILINGS_SOUGHT);

        // Then
        assertThat(filings, is(notNullValue()));
        assertThat(filings.size(), is(FILINGS_SOUGHT.size()));
        assertFilingsSame(filings, FILINGS_SOUGHT);
        assertThat(isSemanticallyEquivalent(filings, FILINGS_EXPECTED), is(true));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments throws 400 Bad Request for an unknown company")
    void getFilingHistoryThrowsBadRequestForUnknownCompany() throws JsonProcessingException {

        final String wireMockPort = environment.getProperty("wiremock.server.port");

        // Given
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://localhost:" + wireMockPort);
        ENVIRONMENT_VARIABLES.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        givenThat(get(urlEqualTo("/company/" + UNKNOWN_COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocuments(UNKNOWN_COMPANY_NUMBER, FILINGS_SOUGHT));
        assertThat(exception.getStatus(), Is.is(BAD_REQUEST));
        final String expectedReason = "Error getting filing history document " + ID_1 +
                " for company number " + UNKNOWN_COMPANY_NUMBER + ".";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments throws 400 Bad Request for an unknown filing history document")
    void getFilingHistoryThrowsBadRequestForUnknownFilingHistoryDocument() throws JsonProcessingException {

        // Given
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + UNKNOWN_ID))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, singletonList(UNKNOWN_FILING)));
        assertThat(exception.getStatus(), Is.is(BAD_REQUEST));
        final String expectedReason = "Error getting filing history document " + UNKNOWN_ID +
                " for company number " + COMPANY_NUMBER + ".";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments throws 500 Internal Server Error for connection failure")
    void getFilingHistoryThrowsInternalServerErrorForForConnectionFailure() {

        final String wireMockPort = environment.getProperty("wiremock.server.port");

        // Given
        ENVIRONMENT_VARIABLES.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        ENVIRONMENT_VARIABLES.set("API_URL", "http://localhost:" + wireMockPort);
        ENVIRONMENT_VARIABLES.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, FILINGS_SOUGHT));
        assertThat(exception.getStatus(), Is.is(INTERNAL_SERVER_ERROR));
        final String expectedReason = "Error sending request to http://localhost:"
                + wireMockPort + "/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1 + ": Connection reset";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    /**
     * Checks that the filings in the two lists are the same by comparing their filing history IDs. Does so in a way
     * that does not require us to add equals() and hashCode() to FilingHistoryDocument.
     * @param filings1 list of filing history documents
     * @param filings2 list of filing history documents
     */
    private void assertFilingsSame(final List<FilingHistoryDocument> filings1,
                                   final List<FilingHistoryDocument> filings2) {
        // Sort lists before comparing - workaround for issue using Hamcrest containsInAnyOrder.
        final List<String> ids1 = filings1.stream().map(FilingHistoryDocument::getFilingHistoryId).collect(toList());
        final List<String> ids2 = filings2.stream().map(FilingHistoryDocument::getFilingHistoryId).collect(toList());
        ids1.sort(String::compareTo);
        ids2.sort(String::compareTo);
        assertThat(ids1, is(ids2));
    }

    /**
     * Factory method that creates an instance of {@link FilingApi} for testing purposes, "reverse-engineered"
     * from the {@link FilingHistoryDocument} provided.
     * @param document the filing history document that should result from the filing this creates
     * @return the filing created
     */
    private static FilingApi filingApi(final FilingHistoryDocument document) {
        final FilingApi filing = new FilingApi();
        filing.setTransactionId(document.getFilingHistoryId());
        filing.setDate(LocalDate.parse(document.getFilingHistoryDate()));
        filing.setDescriptionValues(document.getFilingHistoryDescriptionValues());
        filing.setDescription(document.getFilingHistoryDescription());
        filing.setType(document.getFilingHistoryType());
        return filing;
    }

    /**
     * Serialises the objects to be compared to JSON so that we can compare them without worrying about differences
     * in the type of maps, arrays, etc., underlying their implementations.
     * @param object1 the first object to be compared
     * @param object2 the second object to be compared
     * @return whether the two objects are semantically equivalent (<code>true</code>), or differ (<code>false</code>)
     * @throws JsonProcessingException should something unexpected happen
     */
    private boolean isSemanticallyEquivalent(final Object object1, final Object object2)
            throws JsonProcessingException {
        return objectMapper.writeValueAsString(object1).equals(objectMapper.writeValueAsString(object2));
    }

}


