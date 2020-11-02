package uk.gov.companieshouse.certifiedcopies.orders.api.config;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthorisationInterceptor;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    private static final String CERTIFIED_COPIES_HOME = "/orderable/certified-copies";

    @Mock
    private LoggingInterceptor loggingInterceptor;

    @Mock
    private UserAuthenticationInterceptor userAuthenticationInterceptor;

    @Mock
    private UserAuthorisationInterceptor userAuthorisationInterceptor;

    @Mock
    private CRUDAuthenticationInterceptor crudPermissionInterceptor;

    @Spy
    @InjectMocks
    private ApplicationConfig config;

    @BeforeEach
    void setup() {
        config.CERTIFIED_COPIES_HOME = CERTIFIED_COPIES_HOME;
    }

    @Test
    void addInterceptors() {
        final String expectedAuthPathPattern = CERTIFIED_COPIES_HOME + "/**";

        when(config.crudPermissionsInterceptor()).thenReturn(crudPermissionInterceptor);

        InterceptorRegistry registry = Mockito.mock(InterceptorRegistry.class);

        InterceptorRegistration loggingInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(loggingInterceptorRegistration).when(registry).addInterceptor(loggingInterceptor);

        InterceptorRegistration userAuthenticationInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(userAuthenticationInterceptorRegistration).when(registry).addInterceptor(userAuthenticationInterceptor);

        InterceptorRegistration userAuthorisationInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(userAuthorisationInterceptorRegistration).when(registry).addInterceptor(userAuthorisationInterceptor);

        InterceptorRegistration crudPermissionInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(crudPermissionInterceptorRegistration).when(registry).addInterceptor(crudPermissionInterceptor);

        config.addInterceptors(registry);

        verify(userAuthenticationInterceptorRegistration).addPathPatterns(expectedAuthPathPattern);
        verify(userAuthorisationInterceptorRegistration).addPathPatterns(expectedAuthPathPattern);
        verify(crudPermissionInterceptorRegistration).addPathPatterns(expectedAuthPathPattern);

        InOrder inOrder = Mockito.inOrder(registry);
        inOrder.verify(registry).addInterceptor(loggingInterceptor);
        inOrder.verify(registry).addInterceptor(userAuthenticationInterceptor);
        inOrder.verify(registry).addInterceptor(userAuthorisationInterceptor);
        inOrder.verify(registry).addInterceptor(crudPermissionInterceptor);
    }

}