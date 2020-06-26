package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

import java.util.List;

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
public class FilingHistoryDocumentServiceIntegrationTest {

    private static final List<FilingHistoryDocument> FILINGS_SOUGHT = asList(
            new FilingHistoryDocument(null, null, "MDAxMTEyNzExOGFkaXF6a2N4", null),
            new FilingHistoryDocument(null, null, "MzAwOTM2MDg5OWFkaXF6a2N4", null),
            new FilingHistoryDocument(null, null, "MDE2OTkyOTEwMmFkaXF6a2N4", null),
            new FilingHistoryDocument(null, null, "MDAyNzI3NTQ4OWFkaXF6a2N4", null));

    @Configuration
    @ComponentScan(basePackageClasses = FilingHistoryDocumentServiceIntegrationTest.class)
    static class Config {}

    @Autowired
    private FilingHistoryDocumentService serviceUnderTest;

    @MockBean
    private CertifiedCopyItemService certifiedCopyItemService;

    @Test
    @DisplayName("Gets the expected filing history documents successfully")
    @SetEnvironmentVariable(key = "CHS_API_KEY", value = "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz")
    @SetEnvironmentVariable(key = "API_URL",
            value = "http://api.chs-dev.internal:4001") // TODO GCI-1209 Move to WireMock
    void getFilingHistoryDocuments() {

        // When
        final List<FilingHistoryDocument> filings =
                serviceUnderTest.getFilingHistoryDocuments("00006400", FILINGS_SOUGHT);

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


