package uk.gov.companieshouse.certifiedcopies.orders.api.config;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.util.security.Permission.Key;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthorisationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    @Value("${uk.gov.companieshouse.certifiedcopies.orders.api.home}")
    private String CERTIFIED_COPIES_HOME;

    @Autowired
    private CertifiedCopyItemService certifiedCopyItemService;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
        registry.addInterceptor(new UserAuthenticationInterceptor()).addPathPatterns(CERTIFIED_COPIES_HOME + "/**");
        registry.addInterceptor(new UserAuthorisationInterceptor(certifiedCopyItemService))
                .addPathPatterns(CERTIFIED_COPIES_HOME + "/**");
        registry.addInterceptor(crudPermissionsInterceptor())
                .addPathPatterns(CERTIFIED_COPIES_HOME + "/**");
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(SNAKE_CASE)
                .findAndRegisterModules();
    }

    @Bean
    public CRUDAuthenticationInterceptor crudPermissionsInterceptor() {
        return new CRUDAuthenticationInterceptor(Key.USER_ORDERS);
    }
}
