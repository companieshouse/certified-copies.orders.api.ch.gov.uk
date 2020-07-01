package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filinghistory.FilingResourceHandler;
import uk.gov.companieshouse.api.handler.filinghistory.request.FilingGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.api.error.ApiErrorResponseException.fromHttpResponseException;
import static uk.gov.companieshouse.api.error.ApiErrorResponseException.fromIOException;

/**
 * Unit tests the {@link FilingHistoryDocumentService} class.
 */
@ExtendWith(MockitoExtension.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpResponseException.class)
public class FilingHistoryDocumentServiceTest {

    private static final String COMPANY_NUMBER = "00006400";

    private static final String INVALID_URI = "URI pattern does not match expected URI pattern for this resource.";
    private static final String INVALID_URI_EXPECTED_REASON =
            "Invalid URI /company/00006400/filing-history/1 for filing";

    private static final String IOEXCEPTION_MESSAGE = "IOException thrown by test";
    private static final String IOEXCEPTION_EXPECTED_REASON =
            "Error sending request to http://host/company/00006400/filing-history/1: " + IOEXCEPTION_MESSAGE;

    private static final String NOT_FOUND_EXPECTED_REASON = "Error getting filing history document 1 for company number "
            + COMPANY_NUMBER + ".";

    private static final List<FilingHistoryDocument> FILINGS_SOUGHT = asList(
            new FilingHistoryDocument(null, null, null, "1", null),
            new FilingHistoryDocument(null, null, null, "3", null),
            new FilingHistoryDocument(null, null, null, "4", null),
            new FilingHistoryDocument(null, null, null, "5", null));
    private static final FilingApi FILING_1;
    private static final FilingApi FILING_2;

    static {
        FILING_1 = new FilingApi();
        FILING_1.setDate(LocalDate.now());
        FILING_1.setTransactionId("1");
        FILING_2 = new FilingApi();
        FILING_2.setTransactionId("2");
    }

    @InjectMocks
    private FilingHistoryDocumentService serviceUnderTest;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private FilingResourceHandler resourceHandler;

    @Mock
    private FilingGet filingGet;

    @Mock
    private ApiResponse<FilingApi> response;

    @Mock
    private FilingApi filing;

    @Test
    @DisplayName("isInFilingsSought() finds filing correctly")
    void isInFilingsSought() {
        assertThat(serviceUnderTest.isInFilingsSought(FILING_1, FILINGS_SOUGHT), is(true));
    }

    @Test
    @DisplayName("isInFilingsSought() does not find filing incorrectly")
    void isNotInFilingsSought() {
        assertThat(serviceUnderTest.isInFilingsSought(FILING_2, FILINGS_SOUGHT), is(false));
    }

    @Test
    @DisplayName("validateFilingHistoryDocumentsSought() rejects a null filings request")
    void nullFilingHistoryIsRejected() {
        // When and then
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(() ->
                serviceUnderTest.validateFilingHistoryDocumentsSought(COMPANY_NUMBER, null))
                .withNoCause()
                .withMessage("400 BAD_REQUEST \"No filing history documents requested for company number 00006400. " +
                        "At least one must be requested.\"");
    }

    @Test
    @DisplayName("validateFilingHistoryDocumentsSought() rejects an empty filings request")
    void emptyFilingHistoryIsRejected() {
        // When and then
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(() ->
                serviceUnderTest.validateFilingHistoryDocumentsSought(COMPANY_NUMBER, new ArrayList<>()))
                .withNoCause()
                .withMessage("400 BAD_REQUEST \"No filing history documents requested for company number 00006400. " +
                        "At least one must be requested.\"");
    }

    @Test
    @DisplayName("getFilingHistoryDocuments() reports a URIValidationException as an Internal Server Error (500)")
    void uriValidationExceptionReportedAsServerInternalError() throws Exception  {

        // Given
        setUpForFilingApiException(new URIValidationException(INVALID_URI));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, FILINGS_SOUGHT));
        assertThat(exception.getStatus(), Is.is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getReason(), Is.is(INVALID_URI_EXPECTED_REASON));
    }

    @Test
    @DisplayName("getFilingHistoryDocuments() reports an IOException as an Internal Server Error (500)")
    void serverInternalErrorReportedAsSuch() throws Exception {

        // Given
        setUpForFilingApiException(fromIOException(new IOException(IOEXCEPTION_MESSAGE)));
        when(internalApiClient.getBasePath()).thenReturn("http://host");

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, FILINGS_SOUGHT));
        assertThat(exception.getStatus(), Is.is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getReason(), Is.is(IOEXCEPTION_EXPECTED_REASON));
    }

    /**
     * This is a JUnit 4 test to take advantage of PowerMock.
     * @throws Exception should something unexpected happen
     */
    @org.junit.Test
    public void nonServerInternalErrorResponseReportedAsBadRequest() throws Exception {

        // Given
        final HttpResponseException httpResponseException = PowerMockito.mock(HttpResponseException.class);
        when(httpResponseException.getStatusCode()).thenReturn(404);
        when(httpResponseException.getStatusMessage()).thenReturn("Not Found");
        when(httpResponseException.getHeaders()).thenReturn(new HttpHeaders());
        final ApiErrorResponseException ex = fromHttpResponseException(httpResponseException);
        setUpForFilingApiException(ex);

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getFilingHistoryDocuments(COMPANY_NUMBER, FILINGS_SOUGHT));
        assertThat(exception.getStatus(), Is.is(BAD_REQUEST));
        assertThat(exception.getReason(), Is.is(NOT_FOUND_EXPECTED_REASON));
    }

    /**
     * Provides set up for testing what happens when the Filing API throws an exception during the execution of
     * {@link FilingHistoryDocumentService#getFilingHistoryDocuments(String, List)}.
     * @param exceptionToThrow the exception to throw
     * @throws ApiErrorResponseException should something unexpected happen
     * @throws URIValidationException should something unexpected happen
     */
    private void setUpForFilingApiException(final Exception exceptionToThrow)
            throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.filing()).thenReturn(resourceHandler);
        when(resourceHandler.get("/company/00006400/filing-history/1")).thenReturn(filingGet);
        when(filingGet.execute()).thenThrow(exceptionToThrow);
    }

    /**
     * Provides fair weather set up for testing
     * {@link FilingHistoryDocumentService#getFilingHistoryDocuments(String, List)}.
     * @throws ApiErrorResponseException should something unexpected happen
     * @throws URIValidationException should something unexpected happen
     */
    private void fairWeatherSetUp() throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.filing()).thenReturn(resourceHandler);
        when(resourceHandler.get("/company/00006400/filing-history/1")).thenReturn(filingGet);
        when(filingGet.execute()).thenReturn(response);
        when(response.getData()).thenReturn(filing);
    }

}
