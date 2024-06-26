package uk.gov.companieshouse.certifiedcopies.orders.api.interceptor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerMapping;

import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_OAUTH2_VALUE;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_API_KEY_VALUE;

@ExtendWith(MockitoExtension.class)
class UserAuthorisationInterceptorTest {

    @InjectMocks
    private UserAuthorisationInterceptor userAuthorisationInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private CertifiedCopyItemService service;

    private static final String ITEM_ID = "CHS00000000000000002";
    private static final String ALTERNATIVE_CREATED_BY = "xyz321";
    private static final String INVALID_IDENTITY_TYPE_VALUE = "test";

    @Test
    @DisplayName("Authorise if authenticated user created the certified-copy when request method is GET")
    public void willAuthoriseIfAuthorisedUserCreatedTheCertifiedCopyWhenRequestMethodIsGet() {
        Map<String, String> map = new HashMap<>();
        map.put("id", ITEM_ID);

        CertifiedCopyItem item = new CertifiedCopyItem();
        item.setId(ITEM_ID);
        item.setUserId(ERIC_IDENTITY_VALUE);

        when(request.getMethod()).thenReturn(HttpMethod.GET.toString());
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(map);
        doReturn(ERIC_IDENTITY_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        doReturn(ERIC_IDENTITY_TYPE_OAUTH2_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        when(service.getCertifiedCopyItemById(ITEM_ID)).thenReturn(Optional.of(item));

        Assertions.assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Authorise if request method is POST for a user")
    public void willAuthoriseIfPostAndOAuth2() {
        when(request.getMethod()).thenReturn(HttpMethod.POST.toString());
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn(ERIC_IDENTITY_TYPE_OAUTH2_VALUE);

        Assertions.assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Does not authorise if authenticated user did not create the certified-copy when request method is GET")
    public void doesNotAuthoriseIfAuthenticatedUserDidNotCreateTheCertifiedCopyWhenRequestMethodGet() {
        Map<String, String> map = new HashMap<>();
        map.put("id", ITEM_ID);

        CertifiedCopyItem item = new CertifiedCopyItem();
        item.setId(ITEM_ID);
        item.setUserId(ALTERNATIVE_CREATED_BY);

        when(request.getMethod()).thenReturn(HttpMethod.GET.toString());
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(map);
        doReturn(ERIC_IDENTITY_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        doReturn(ERIC_IDENTITY_TYPE_OAUTH2_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        when(service.getCertifiedCopyItemById(ITEM_ID)).thenReturn(Optional.of(item));

        Assertions.assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Does not Authorise if request method is GET and there is no user")
    public void willNotAuthoriseIfMethodIsGetAndNoIdentity() {
        when(request.getMethod()).thenReturn(HttpMethod.POST.toString());
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn(ERIC_IDENTITY_TYPE_OAUTH2_VALUE);

        Assertions.assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Does not authorise if certified-copy is not found when request method is GET for a user")
    public void willNotAuthoriseIfCertifiedCopyIsNotFoundAndOAuth2() {
        Map<String, String> map = new HashMap<>();
        map.put("id", ITEM_ID);

        when(request.getMethod()).thenReturn(HttpMethod.GET.toString());
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(map);
        doReturn(ERIC_IDENTITY_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        doReturn(ERIC_IDENTITY_TYPE_OAUTH2_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        when(service.getCertifiedCopyItemById(ITEM_ID)).thenReturn(Optional.empty());

        Assertions.assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Does not Authorise an external API key is used")
    public void willNotAuthoriseIfRequestIsExternalAPIKey() {
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn(ERIC_IDENTITY_TYPE_API_KEY_VALUE);
        Assertions.assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Authorise if GET and an internal API key is used")
    public void willAuthoriseIfRequestIsGetAndInternalAPIKey() {
        when(request.getMethod()).thenReturn(HttpMethod.GET.toString());
        doReturn("request-id").when(request).getHeader("X-Request-ID");
        doReturn(ERIC_IDENTITY_TYPE_API_KEY_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        doReturn(SecurityConstants.INTERNAL_USER_ROLE).when(request).getHeader(EricConstants.ERIC_AUTHORISED_KEY_ROLES);
        Assertions.assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Does not Authorise if POST and an internal API key is used")
    public void willNotAuthoriseIfRequestIsPostAndInternalAPIKey() {
        when(request.getMethod()).thenReturn(HttpMethod.POST.toString());
        doReturn("request-id").when(request).getHeader("X-Request-ID");
        doReturn(ERIC_IDENTITY_TYPE_API_KEY_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        doReturn(SecurityConstants.INTERNAL_USER_ROLE).when(request).getHeader(EricConstants.ERIC_AUTHORISED_KEY_ROLES);
        Assertions.assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }

    @Test
    @DisplayName("Does not Authorise if POST and unrecognised identity type")
    public void willNotAuthoriseIfRequestIsPostAndUnrecognisedIdentity() {
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn(INVALID_IDENTITY_TYPE_VALUE);
        Assertions.assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }
}
