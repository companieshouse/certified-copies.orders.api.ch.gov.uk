package uk.gov.companieshouse.certifiedcopies.orders.api.logging;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LoggingUtils {

    private LoggingUtils() { }
    
    public static final String APPLICATION_NAMESPACE = "certified-copies.orders.api.ch.gov.uk";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String USER_ID_LOG_KEY = "user_id";
    public static final String COMPANY_NUMBER_LOG_KEY = "company_number";
    public static final String CERTIFIED_COPY_ID_LOG_KEY = "certified_copy_id";
    public static final String STATUS_LOG_KEY = "status";
    public static final String REQUEST_ID_LOG_KEY = "request_id";

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

}
