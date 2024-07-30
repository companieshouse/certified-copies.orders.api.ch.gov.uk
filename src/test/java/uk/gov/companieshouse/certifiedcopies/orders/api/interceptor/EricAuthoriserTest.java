package uk.gov.companieshouse.certifiedcopies.orders.api.interceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.ERIC_AUTHORISED_ROLES;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.StringHelper;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EricAuthoriserTest {

    private static final String FREE_CERT_DOCS_PERMISSION= "/admin/free-cert-docs";
    private static final String FREE_CERT_DOCS_HEADER_VALUE= "/admin/free-cert-docs";

    @Mock
    private StringHelper stringHelper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private EricAuthoriser authoriser;

    @Test
    @DisplayName("Should return true when permission is present in the header")
    void shouldReturnTrueForValidPermission() {
        final String headerValue = FREE_CERT_DOCS_HEADER_VALUE;
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(headerValue);
        when(stringHelper.asSet("\\s+", headerValue)).thenReturn(Set.of("/admin/free-cert-docs"));
        boolean hasPermission = authoriser.hasPermission(FREE_CERT_DOCS_PERMISSION, request);

        assertTrue(hasPermission, "User should have permission");

        verify(request).getHeader(ERIC_AUTHORISED_ROLES);
        verify(stringHelper).asSet("\\s+", headerValue);
    }

    @Test
    @DisplayName("Should return false when free permission is not present in the header")
    void shouldReturnFalseForInvalidPermission() {
        final String headerValue = "non-admin";
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(headerValue);
        when(stringHelper.asSet("\\s+", headerValue)).thenReturn(Set.of("non-admin"));
        boolean hasPermission = authoriser.hasPermission(FREE_CERT_DOCS_PERMISSION, request);

        assertFalse(hasPermission, "User should not have permission");
    }

    @Test
    @DisplayName("Should return false when the header is null")
    void shouldReturnFalseWhenHeaderIsMissing() {
        when(request.getHeader(ERIC_AUTHORISED_ROLES)).thenReturn(null);
        boolean hasPermission = authoriser.hasPermission(FREE_CERT_DOCS_PERMISSION, request);

        assertFalse(hasPermission, "User should not have permission when header is missing");
    }
}
