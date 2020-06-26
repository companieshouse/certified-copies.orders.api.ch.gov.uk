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

    // TODO GCI-1209 Is the company number entirely redundant in this context?
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
        // TODO GCI-1209 Validate inputs?
        final ApiClient apiClient = apiClientService.getInternalApiClient();
        final String uri = GET_FILING_HISTORY.expand(companyNumber).toString();
        try {
                // TODO GCI-1209 Can we use query params to make this less inefficient?
                final FilingHistoryApi history = apiClient.filingHistory().list(uri).execute().getData();
                // TODO GCI-1209 Remove this
                history.getItems().forEach(filing -> System.out.println(filing.getTransactionId() + ":" + filing.getDescription()));

                // TODO GCI-1209 date format etc?
                return history.getItems().stream().
                        filter(filing -> isInFilingsSought(filing, filingHistoryDocumentsSought)).
                        map(filing ->
                        new FilingHistoryDocument(filing.getDate().toString(),
                                                  filing.getDescription(),
                                                  filing.getTransactionId(),
                                                  filing.getType())).collect(toList());
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

    // TODO GCI-1209 Sort out error messages, Javadoc, etc.
    /**
     * Creates an appropriate exception to report the underlying problem.
     * @param apiException the API exception caught
     * @param client the API client
     * @param companyNumber the number of the company looked up
     * @param uri the URI used to communicate with the company profiles API
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
            final String error = "Error getting company name for company number " + companyNumber;
            LOGGER.error(error, apiException);
            propagatedException =  new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        }
        return propagatedException;
    }

}
