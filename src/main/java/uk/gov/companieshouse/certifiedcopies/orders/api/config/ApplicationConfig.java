package uk.gov.companieshouse.certifiedcopies.orders.api.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.interceptor.UserAuthorisationInterceptor;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    @Value("${uk.gov.companieshouse.certifiedcopies.orders.api.path.pattern}")
    private String CERTIFIED_COPIES_PATH_PATTERN;

    @Autowired
    private CertifiedCopyItemService certifiedCopyItemService;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
        registry.addInterceptor(new UserAuthenticationInterceptor()).addPathPatterns(CERTIFIED_COPIES_PATH_PATTERN);
        registry.addInterceptor(new UserAuthorisationInterceptor(certifiedCopyItemService))
                .addPathPatterns(CERTIFIED_COPIES_PATH_PATTERN);
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

}
