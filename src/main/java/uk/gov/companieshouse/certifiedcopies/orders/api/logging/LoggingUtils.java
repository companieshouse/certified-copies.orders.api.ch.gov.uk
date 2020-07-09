package uk.gov.companieshouse.certifiedcopies.orders.api.logging;

import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class LoggingUtils {

    private LoggingUtils() { }
    
    public static final String APPLICATION_NAMESPACE = "certified-copies.orders.api.ch.gov.uk";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String USER_ID_LOG_KEY = "user_id";
    public static final String COMPANY_NUMBER_LOG_KEY = "company_number";
    public static final String CERTIFIED_COPY_ID_LOG_KEY = "certified_copy_id";
    public static final String STATUS_LOG_KEY = "status";
    public static final String ERRORS_LOG_KEY = "errors";
    public static final String REQUEST_ID_LOG_KEY = "request_id";
    public static final String IDENTITY_LOG_KEY = "identity";
    public static final String DESCRIPTION_LOG_KEY = "description_key";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * method to set up a map for logging purposes and add a value for the
     * request id
     *
     * @param requestId
     * @return
     */
    public static Map<String, Object> createLoggingDataMap(final String requestId) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, requestId);
        return logMap;
    }

    /**
     * Method to set up a map for logging purposes and add a value for the
     * company number.
     *
     * @param companyNumber the company number to log under the key {@link LoggingUtils#COMPANY_NUMBER_LOG_KEY}
     * @return the log map for use in log messages
     */
    public static Map<String, Object> createLogMapWithCompanyNumber(final String companyNumber) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(COMPANY_NUMBER_LOG_KEY, companyNumber);
        return logMap;
    }

    /**
     * method to add errors and a status to a map for logging
     * purposes
     * @param logMap the map of logging data
     * @param errors a list of errors
     */
    public static void logErrorsWithStatus(final Map<String, Object> logMap,
                                           final List<String> errors,
                                           final HttpStatus status) {
        logMap.put(ERRORS_LOG_KEY, errors);
        logMap.put(STATUS_LOG_KEY, status);
    }

    /**
     * Method to add an error and a status to a map for logging
     * purposes.
     * @param logMap the map of logging data
     * @param error error message
     */
    public static void logErrorWithStatus(final Map<String, Object> logMap,
                                          final String error,
                                          final HttpStatus status) {
        logMap.put(ERRORS_LOG_KEY, singletonList(error));
        logMap.put(STATUS_LOG_KEY, status);
    }

    /**
     * Method to log an error and add a value for the description key.
     *
     * @param descriptionKey the company number to log under the key {@link LoggingUtils#DESCRIPTION_LOG_KEY}
     * @param errorMessage the error message to display
     */
    public static void logOrdersDescriptionsConfigError(final String descriptionKey, final String errorMessage) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(DESCRIPTION_LOG_KEY, descriptionKey);
        LOGGER.error(errorMessage);
    }
}
