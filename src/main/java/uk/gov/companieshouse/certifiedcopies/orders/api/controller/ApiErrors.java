package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.ApiErrorBuilder;

/**
 * Wraps up status and list of messages for rendering in non-2xx REST response payload.
 */
public class ApiErrors {

    private static final String JSON_PROCESSING_ERROR = "json-processing-error";
    private static final String JSON_PROCESSING_LOCATION = "certified_copy_item";
    private static final String ERROR_TYPE_SERVICE = "ch:service";
    private static final String CERTIFICATE_NOT_FOUND_ERROR = "certified-copy-not-found-error";
    private static final String QUANTITY_AMOUNT_ERROR = "quantity-error";
    private static final String QUANTITY_LOCATION = "quantity";

    private static final String ID_LOCATION = "id";
    public static final String OBJECT_LOCATION_TYPE = "object";
    public static final String ERROR_TYPE_VALIDATION = "ch:validation";

    public static final String STRING_LOCATION_TYPE = "string";

    public static final ApiError ERR_JSON_PROCESSING = new ApiError(JSON_PROCESSING_ERROR, JSON_PROCESSING_LOCATION, OBJECT_LOCATION_TYPE, ERROR_TYPE_SERVICE);
    public static final ApiError ERR_CERTIFIED_COPY_NOT_FOUND = new ApiError(CERTIFICATE_NOT_FOUND_ERROR, ID_LOCATION, STRING_LOCATION_TYPE, ERROR_TYPE_VALIDATION);
    public static final ApiError ERR_QUANTITY_AMOUNT = new ApiError(QUANTITY_AMOUNT_ERROR, QUANTITY_LOCATION, OBJECT_LOCATION_TYPE, ERROR_TYPE_VALIDATION);


    private final HttpStatus status;
    private final List<String> errors;

    public ApiErrors(final HttpStatus status, final List<String> errors) {
        super();
        this.status = status;
        this.errors = errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }

    public static ResponseEntity<Object> errorResponse(HttpStatus httpStatus, ApiError apiError) {
        return ResponseEntity.status(httpStatus).body(new ApiResponse<>(Collections.singletonList(apiError)));
    }

    public static ResponseEntity<Object> errorResponse(HttpStatus httpStatus, List<ApiError> errors) {
        return ResponseEntity.status(httpStatus).body(new ApiResponse<>(errors));
    }

    public static ApiError raiseError(ApiError apiError, String errorMessage, Object ...objects) {
        return ApiErrorBuilder
                .builder(apiError).withErrorMessage(String.format(errorMessage, objects)).build();
    }
}
