package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.api.model.filinghistory.FilingHistoryApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Integrations tests the {@link FilingHistoryDocumentService}.
 */
@SpringBootTest
@SpringJUnitConfig(FilingHistoryDocumentServiceIntegrationTest.Config.class)
@AutoConfigureWireMock(port = 12345) // TODO GCI-1209 randomly allocated port?
public class FilingHistoryDocumentServiceIntegrationTest {


    private static final String COMPANY_NUMBER = "00006400";
    private static final String ID_1 = "MDAxMTEyNzExOGFkaXF6a2N4";
    private static final String ID_2 = "MzAwOTM2MDg5OWFkaXF6a2N4";
    private static final String ID_3 = "MDE2OTkyOTEwMmFkaXF6a2N4";
    private static final String ID_4 = "MDAyNzI3NTQ4OWFkaXF6a2N4";

    private static final List<FilingHistoryDocument> FILINGS_SOUGHT = asList(
            new FilingHistoryDocument(null, null, ID_1, null),
            new FilingHistoryDocument(null, null, ID_2, null),
            new FilingHistoryDocument(null, null, ID_3, null),
            new FilingHistoryDocument(null, null, ID_4, null));

    private static final FilingHistoryApi FILING_HISTORY;

    static {
        FILING_HISTORY = new FilingHistoryApi();

        final FilingApi filing1 = new FilingApi();
        filing1.setTransactionId(ID_1);
        filing1.setDate(LocalDate.now());
        filing1.setDescription("");
        filing1.setType("");
        final FilingApi filing2 = new FilingApi();
        filing2.setTransactionId(ID_2);
        filing2.setDate(LocalDate.now());
        filing2.setDescription("");
        filing2.setType("");
        final FilingApi filing3 = new FilingApi();
        filing3.setTransactionId(ID_3);
        filing3.setDate(LocalDate.now());
        filing3.setDescription("");
        filing3.setType("");
        final FilingApi filing4 = new FilingApi();
        filing4.setTransactionId(ID_4);
        filing4.setDate(LocalDate.now());
        filing4.setDescription("");
        filing4.setType("");
        final List<FilingApi> filings = asList(filing1, filing2, filing3, filing4);
        FILING_HISTORY.setItems(filings);
    }

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

    @MockBean
    private CertifiedCopyItemService certifiedCopyItemService;

    @Test
    @DisplayName("Gets the expected filing history documents successfully")
    @SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
    @SetEnvironmentVariable(key = "API_URL", value = "http://localhost:12345") // TODO GCI-1209 randomly allocated port?
    void getFilingHistoryDocuments() throws JsonProcessingException {

        // Given
        givenThat(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_HISTORY))));

        // When
        final List<FilingHistoryDocument> filings =
                serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, FILINGS_SOUGHT);

        // Then
        assertThat(filings, is(notNullValue()));
        assertThat(filings.size(), is(FILINGS_SOUGHT.size()));
        assertFilingsSame(filings, FILINGS_SOUGHT);
        assertFilingsArePopulated(filings);
    }

    /**
     * Checks that the filings in the two lists are the same by comparing their filing history IDs. Does so in a way
     * that does not require use to add equals() and hashCode() to FilingHistoryDocument.
     * @param filings1 list of filing history documents
     * @param filings2 list of filing history documents
     */
    private void assertFilingsSame(final List<FilingHistoryDocument> filings1, final List<FilingHistoryDocument> filings2) {
        // Sort lists before comparing - workaround for issue using Hamcrest containsInAnyOrder.
        final List<String> ids1 = filings1.stream().map(FilingHistoryDocument::getFilingHistoryId).collect(toList());
        final List<String> ids2 = filings2.stream().map(FilingHistoryDocument::getFilingHistoryId).collect(toList());
        ids1.sort(String::compareTo);
        ids2.sort(String::compareTo);
        assertThat(ids1, is(ids2));
    }

    /**
     * Checks that each of the filings provided has all of its fields populated.
     * @param filings the filing history documents to check
     */
    private void assertFilingsArePopulated(final List<FilingHistoryDocument> filings) {
        filings.forEach(filing -> assertThat(filing.getFilingHistoryId() != null &&
                                             filing.getFilingHistoryDescription() != null &&
                                             filing.getFilingHistoryDate() != null &&
                                             filing.getFilingHistoryType() != null, is(true)));
    }

}

