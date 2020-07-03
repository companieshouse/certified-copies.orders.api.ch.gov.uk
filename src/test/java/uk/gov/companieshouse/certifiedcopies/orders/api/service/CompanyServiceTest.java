package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.api.error.ApiErrorResponseException.fromHttpResponseException;
import static uk.gov.companieshouse.api.error.ApiErrorResponseException.fromIOException;

@ExtendWith(MockitoExtension.class)
@PrepareForTest(HttpResponseException.class)
public class CompanyServiceTest {

    private static final String COMPANY_NUMBER = "00006400";
    private static final String COMPANY_NAME = "Company Name";

    private static final String INVALID_URI = "URI pattern does not match expected URI pattern for this resource.";
    private static final String INVALID_URI_EXPECTED_REASON = "Invalid URI /company/00006400 for company details";

    private static final String IOEXCEPTION_MESSAGE = "IOException thrown by test";
    private static final String IOEXCEPTION_EXPECTED_REASON =
            "Error sending request to http://host/company/00006400: " + IOEXCEPTION_MESSAGE;

    @InjectMocks
    private CompanyService serviceUnderTest;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient apiClient;

    @Mock
    private CompanyResourceHandler handler;

    @Mock
    private CompanyGet get;

    @Test
    @DisplayName("getCompanyName() returns a string when api call is successful")
    public void getCompanyNameIsValid() throws Exception {

        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyName(COMPANY_NAME);
        ApiResponse<CompanyProfileApi> apiResponse = new ApiResponse<>(200, new HashMap<>(), companyProfileApi);

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(handler);
        when(handler.get(anyString())).thenReturn(get);
        when(get.execute()).thenReturn(apiResponse);

        String result = serviceUnderTest.getCompanyName(COMPANY_NUMBER);
        assertThat(result, is(COMPANY_NAME));
    }

    @Test
    @DisplayName("getCompanyName() Invalid URL reported as Internal Server Error (500)")
    public void getCompanyNameThrowsInternalServerErrorForInvalidUri() throws Exception {

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(handler);
        when(handler.get(anyString())).thenReturn(get);
        when(get.execute()).thenThrow(new URIValidationException(INVALID_URI));

        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getCompanyName(COMPANY_NUMBER));
        assertThat(exception.getStatus(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getReason(), is(INVALID_URI_EXPECTED_REASON));
    }

    @Test
    @DisplayName("getCompanyName() ApiErrorResponseException Internal Server Error is reported as such (500)")
    public void getCompanyNameInternalServerErrorApiExceptionIsPropagated() throws Exception {

        final IOException ioException = new IOException(IOEXCEPTION_MESSAGE);

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(handler);
        when(handler.get(anyString())).thenReturn(get);
        when(get.execute()).thenThrow(fromIOException(ioException));
        when(apiClient.getBasePath()).thenReturn("http://host");

        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getCompanyName(COMPANY_NUMBER));
        assertThat(exception.getStatus(), is(INTERNAL_SERVER_ERROR));
        assertThat(exception.getReason(), is(IOEXCEPTION_EXPECTED_REASON));
    }

    @Test
    @DisplayName("getCompanyName() ApiErrorResponseException any other status code is reported as bad request (400)")
    public void getCompanyNameInternalServerErrorApiExceptionWithAnyOtherStatusCodeIsPropagated() throws Exception {
        HttpResponseException httpResponseException = new HttpResponseException
                .Builder(404, "Not Found", new HttpHeaders()).build();

        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(handler);
        when(handler.get(anyString())).thenReturn(get);
        when(get.execute()).thenThrow(fromHttpResponseException(httpResponseException));

        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getCompanyName(COMPANY_NUMBER));
        assertThat(exception.getStatus(), is(BAD_REQUEST));
    }

}
