package uk.gov.companieshouse.certifiedcopies.orders.api.controller;

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.FieldNameConverter;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final FieldNameConverter converter;

    public GlobalExceptionHandler(FieldNameConverter converter) {
        this.converter = converter;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull final MethodArgumentNotValidException ex,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpStatusCode status,
            @NonNull final WebRequest request) {
        final ApiErrors apiError = buildBadRequestApiError(ex);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull final HttpMessageNotReadableException ex,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpStatusCode status,
            @NonNull final WebRequest request) {

        if (ex.getCause() instanceof JsonProcessingException) {
            final ApiErrors apiError = buildBadRequestApiError((JsonProcessingException) ex.getCause());
            return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
        }

        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    /**
     * Utility to build ApiError from MethodArgumentNotValidException.
     * @param ex the MethodArgumentNotValidException handled
     * @return the resulting ApiError
     */
    private ApiErrors buildBadRequestApiError(final MethodArgumentNotValidException ex) {
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(converter.toSnakeCase(error.getField()) + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        return new ApiErrors(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Utility to build ApiError from JsonProcessingException.
     * @param jpe the JsonProcessingException handled
     * @return the resulting ApiError
     */
    private ApiErrors buildBadRequestApiError(final JsonProcessingException jpe) {
        final String errorMessage = jpe.getOriginalMessage();
        return new ApiErrors(HttpStatus.BAD_REQUEST, singletonList(errorMessage));
    }

}
