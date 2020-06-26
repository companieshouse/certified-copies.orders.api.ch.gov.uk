package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.api.model.filinghistory.FilingHistoryApi;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class FilingHistoryDocumentService {

    private static final Logger LOGGER = LoggingUtils.getLogger();

    private static final UriTemplate
            GET_FILING_HISTORY =
            new UriTemplate("/company/{companyNumber}/filing-history");

    private final ApiClientService apiClientService;

    public FilingHistoryDocumentService(final ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    // TODO GCI-1209 Is the spec for FilingHistoryDocument going to change effectively?
    /**
     * Gets fully populated filing history documents for the partially populated filing history documents provided.
     * @param companyNumber the company number
     * @param filingHistoryDocumentsSought the filing history documents, assumed to have their IDs populated at least
     * @return fully populated documents
     */
    public List<FilingHistoryDocument> getFilingHistoryDocuments(
            final String companyNumber,
            final List<FilingHistoryDocument> filingHistoryDocumentsSought) {
        LOGGER.info(filingHistoryDocumentsSought.size() + " filing history document(s) sought for company number "
                + companyNumber + ".");
        final ApiClient apiClient = apiClientService.getInternalApiClient();
        final String uri = GET_FILING_HISTORY.expand(companyNumber).toString();
        try {
                // TODO GCI-1209 Can we use query params to make this less inefficient?
                final FilingHistoryApi history = apiClient.filingHistory().list(uri).execute().getData();
                LOGGER.info("Filing history returned for company number " + companyNumber +
                        " contains " + history.getItems().size() + " document(s).");
                // TODO GCI-1209 date format etc?
                final List<FilingHistoryDocument> filings = history.getItems().stream().
                        filter(filing -> isInFilingsSought(filing, filingHistoryDocumentsSought)).
                        map(filing ->
                                new FilingHistoryDocument(filing.getDate().toString(),
                                        filing.getDescription(),
                                        filing.getTransactionId(),
                                        filing.getType())).collect(toList());
                LOGGER.info("Returning " + filings.size() +
                        " matching filing history document(s) for company number " + companyNumber + ".");
                return filings;
            } catch (ApiErrorResponseException ex) {
                throw getResponseStatusException(ex, apiClient, companyNumber, uri);
            } catch (URIValidationException ex) {
                // Should this happen (unlikely), it is a broken contract, hence 500.
                final String error = "Invalid URI " + uri + " for filing history";
                LOGGER.error(error, ex);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error);
            }

    }

    /**
     * Indicates whether the filing provided is amongst those sought.
     * @param filing the filing to check
     * @param filingHistoryDocumentsSought the filings sought
     * @return <code>true</code> where the filing is one of those sought, <code>false</code> otherwise
     */
    boolean isInFilingsSought(final FilingApi filing,
                              final List<FilingHistoryDocument> filingHistoryDocumentsSought) {
        final List<String> filingHistoryIds = filingHistoryDocumentsSought.stream()
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

        final ResponseStatusException propagatedException;
        if (apiException.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            final String error = "Error sending request to "
                    + client.getBasePath() + uri + ": " + apiException.getStatusMessage();
            LOGGER.error(error, apiException);
            propagatedException = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, error);
        } else {
            final String error = "Error getting filing history for company number " + companyNumber + ".";
            LOGGER.error(error, apiException);
            propagatedException =  new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        }
        return propagatedException;
    }

}
