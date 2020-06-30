package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.createLogMapWithCompanyNumber;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.logErrorWithStatus;

@Service
public class FilingHistoryDocumentService {

    private static final Logger LOGGER = LoggingUtils.getLogger();

    private static final UriTemplate
            GET_FILING_HISTORY_DOCUMENT =
            new UriTemplate("/company/{companyNumber}/filing-history/{filingHistoryId}");

    private final ApiClientService apiClientService;

    public FilingHistoryDocumentService(final ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * Gets fully populated filing history documents for the partially populated filing history documents provided.
     * @param companyNumber the company number
     * @param filingHistoryDocumentsRequested the filing history documents requested, assumed to have their IDs
     *                                        populated at least, and of which there must be at least one
     * @return fully populated documents
     */
    public List<FilingHistoryDocument> getFilingHistoryDocuments(
            final String companyNumber,
            final List<FilingHistoryDocument> filingHistoryDocumentsRequested) {

        validateFilingHistoryDocumentsSought(companyNumber, filingHistoryDocumentsRequested);
        final Map<String, Object> logMap = createLogMapWithCompanyNumber(companyNumber);
        LOGGER.info(filingHistoryDocumentsRequested.size() + " filing history document(s) requested for company number "
                + companyNumber + ".", logMap);
        final List<FilingHistoryDocument> filings =
                filingHistoryDocumentsRequested.stream()
                .map(filing -> getFilingHistoryDocument(companyNumber, filing.getFilingHistoryId()))
                .collect(toList());
        LOGGER.info("Returning " + filings.size() + " filing history document(s) for company number "
                + companyNumber + ".", logMap);
        return filings;
    }

    /**
     * Gets the fully populated filing history document for the filing history document ID provided.
     * @param companyNumber the company number
     * @param filingHistoryDocumentId the filing history document ID
     * @return fully populated document
     */
    public FilingHistoryDocument getFilingHistoryDocument(
            final String companyNumber,
            final String filingHistoryDocumentId) {

        final Map<String, Object> logMap = createLogMapWithCompanyNumber(companyNumber);
        LOGGER.info("Getting filing history document " + filingHistoryDocumentId + " for company number "
                + companyNumber + ".", logMap);
        final ApiClient apiClient = apiClientService.getInternalApiClient();
        final String uri = GET_FILING_HISTORY_DOCUMENT.expand(companyNumber, filingHistoryDocumentId).toString();
        try {
            final FilingApi filing = apiClient.filing().get(uri).execute().getData();
            return new FilingHistoryDocument(filing.getDate().toString(),
                    filing.getDescription(),
                    filing.getDescriptionValues(),
                    filing.getTransactionId(),
                    filing.getType());
        } catch (ApiErrorResponseException ex) {
            throw getResponseStatusException(ex, apiClient, companyNumber, uri);
        } catch (URIValidationException ex) {
            // Should this happen (unlikely), it is a broken contract, hence 500.
            final String error = "Invalid URI " + uri + " for filing history";
            logErrorWithStatus(logMap, error, INTERNAL_SERVER_ERROR);
            LOGGER.error(error, ex, logMap);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }

    }

    /**
     * Validates that at least one filing history document (filing) has been requested.
     * @param companyNumber the company for which the filings have been requested
     * @param filingHistoryDocumentsRequested the filings requested
     */
    void validateFilingHistoryDocumentsSought(final String companyNumber,
                                              final List<FilingHistoryDocument> filingHistoryDocumentsRequested) {
        if (isEmpty(filingHistoryDocumentsRequested)) {
            final String error = "No filing history documents requested for company number " + companyNumber
                    + ". At least one must be requested.";
            final Map<String, Object> logMap = createLogMapWithCompanyNumber(companyNumber);
            logErrorWithStatus(logMap, error, BAD_REQUEST);
            LOGGER.error(error, logMap);
            throw new ResponseStatusException(BAD_REQUEST, error);
        }
    }

    /**
     * Indicates whether the filing provided is amongst those requested.
     * @param filing the filing to check
     * @param filingHistoryDocumentsRequested the filings requested
     * @return <code>true</code> where the filing is one of those sought, <code>false</code> otherwise
     */
    boolean isInFilingsSought(final FilingApi filing,
                              final List<FilingHistoryDocument> filingHistoryDocumentsRequested) {
        final List<String> filingHistoryIds = filingHistoryDocumentsRequested.stream()
                        .map(FilingHistoryDocument::getFilingHistoryId)
                        .collect(toList());
        return filingHistoryIds.contains(filing.getTransactionId());
    }

    /**
     * Creates an appropriate exception to report the underlying problem.
     * @param apiException the API exception caught
     * @param client the API client
     * @param companyNumber the number of the company for which the filing history is looked up
     * @param uri the URI used to communicate with the company filing history API
     * @return the {@link ResponseStatusException} exception to report the problem
     */
    private ResponseStatusException getResponseStatusException(final ApiErrorResponseException apiException,
                                                               final ApiClient client,
                                                               final String companyNumber,
                                                               final String uri) {

        final Map<String, Object> logMap = createLogMapWithCompanyNumber(companyNumber);
        final ResponseStatusException propagatedException;
        if (apiException.getStatusCode() == INTERNAL_SERVER_ERROR.value()) {
            final String error = "Error sending request to "
                    + client.getBasePath() + uri + ": " + apiException.getStatusMessage();
            logErrorWithStatus(logMap, error, INTERNAL_SERVER_ERROR);
            LOGGER.error(error, apiException, logMap);
            propagatedException = new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        } else {
            final String error = "Error getting filing history for company number " + companyNumber + ".";
            logErrorWithStatus(logMap, error, BAD_REQUEST);
            LOGGER.error(error, apiException, logMap);
            propagatedException =  new ResponseStatusException(BAD_REQUEST, error);
        }
        return propagatedException;
    }

}
