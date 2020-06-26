package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests the {@link FilingHistoryDocumentService} class.
 */
@ExtendWith(MockitoExtension.class)
public class FilingHistoryDocumentServiceTest {

    private static final List<FilingHistoryDocument> FILINGS_SOUGHT = asList(
            new FilingHistoryDocument(null, null, "1", null),
            new FilingHistoryDocument(null, null, "3", null),
            new FilingHistoryDocument(null, null, "4", null),
            new FilingHistoryDocument(null, null, "5", null));
    private static final FilingApi FILING_1;
    private static final FilingApi FILING_2;

    static {
        FILING_1 = new FilingApi();
        FILING_1.setTransactionId("1");
        FILING_2 = new FilingApi();
        FILING_2.setTransactionId("2");
    }

    @InjectMocks
    private FilingHistoryDocumentService serviceUnderTest;

    @Mock
    private ApiClientService apiClientService;

    @Test
    void isInFilingsSought() {
        assertThat(serviceUnderTest.isInFilingsSought(FILING_1, FILINGS_SOUGHT), is(true));
    }

    @Test
    void isNotInFilingsSought() {
        assertThat(serviceUnderTest.isInFilingsSought(FILING_2, FILINGS_SOUGHT), is(false));
    }

}
