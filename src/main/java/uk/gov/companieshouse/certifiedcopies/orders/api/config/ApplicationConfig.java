package uk.gov.companieshouse.certifiedcopies.orders.api.config;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.util.security.Permission.Key;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthorisationInterceptor;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    @Value("${uk.gov.companieshouse.certifiedcopies.orders.api.home}")
    String certifiedCopiesHome;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private UserAuthenticationInterceptor userAuthenticationInterceptor;
    
    @Autowired
    private UserAuthorisationInterceptor userAuthorisationInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        final String authPathPattern = certifiedCopiesHome + "/**";
        registry.addInterceptor(userAuthenticationInterceptor).addPathPatterns(authPathPattern);
        registry.addInterceptor(userAuthorisationInterceptor).addPathPatterns(authPathPattern);
        registry.addInterceptor(crudPermissionsInterceptor()).addPathPatterns(authPathPattern);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(SNAKE_CASE)
                .findAndRegisterModules();
    }

    @Bean
    public CRUDAuthenticationInterceptor crudPermissionsInterceptor() {
        return new CRUDAuthenticationInterceptor(Key.USER_ORDERS);
    }
}
