package uk.gov.companieshouse.certifiedcopies.orders.api.logging;

public class LoggingUtils {

    private LoggingUtils() { }
    
    public static final String APPLICATION_NAMESPACE = "certified-copies.orders.api.ch.gov.uk";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String USER_ID_LOG_KEY = "user_id";
    public static final String COMPANY_NUMBER_LOG_KEY = "company_number";
    public static final String CERTIFIED_COPY_ID_LOG_KEY = "certified_copy_id";
    public static final String STATUS_LOG_KEY = "status";
    public static final String REQUEST_ID_LOG_KEY = "request_id";

}
