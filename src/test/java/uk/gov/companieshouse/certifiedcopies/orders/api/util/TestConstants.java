package uk.gov.companieshouse.certifiedcopies.orders.api.util;

import static java.util.Collections.singletonList;

public class TestConstants {
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String REQUEST_ID_VALUE = "f058ebd6-02f7-4d3f-942e-904344e8cde5";
    public static final String ERIC_IDENTITY_VALUE = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";
    public static final String ERIC_IDENTITY_TYPE_OAUTH2_VALUE = "oauth2";
    public static final String ERIC_IDENTITY_HEADER_NAME = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE_HEADER_NAME = "ERIC-Identity-Type";
    public static final String ERIC_IDENTITY_TYPE_API_KEY_VALUE = "key";
    public static final String ERIC_AUTHORISED_ROLES = "ERIC-Authorised-Roles";
    public static final int CERTIFIED_COPY_COST = 15;
    public static final int SAME_DAY_CERTIFIED_COPY_COST = 50;
    public static final int DIGITAL_CERTIFIED_COPY_COST = 9;
    public static final int DIGITAL_CERTIFIED_COPY_NEW_INCORPORATION_COST = 9;
    public static final int CERTIFIED_COPY_NEW_INCORPORATION_COST = 30;
    public static final int SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST = 100;
    public static final String CERTIFIED_COPIES_URL = "/orderable/certified-copies";
    public static final ApiErrorResponsePayload FILING_NOT_FOUND =
            new ApiErrorResponsePayload(singletonList(new Error("ch:service", "filing-history-item-not-found")));
    public static final String FILING_HISTORY_TYPE_CH01 = "CH01";
    public static final String FILING_HISTORY_TYPE_NEWINC = "NEWINC";
    public static final String TOKEN_PERMISSION_VALUE = "user_orders=%s";
}
