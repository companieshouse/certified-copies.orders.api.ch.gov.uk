package uk.gov.companieshouse.certifiedcopies.orders.api.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.service.CertifiedCopyItemService;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.EricHeaderHelper;
import uk.gov.companieshouse.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.IDENTITY_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.USER_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.CERTIFIED_COPY_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.STATUS_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.getLogger;

@Component
public class UserAuthorisationInterceptor implements HandlerInterceptor {

    private final CertifiedCopyItemService service;

    private static final Logger LOGGER = getLogger();

    public UserAuthorisationInterceptor(CertifiedCopyItemService service) {
        this.service = service;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final String identityType = EricHeaderHelper.getIdentityType(request);
        boolean isApiKeyRequest = identityType.equals(EricHeaderHelper.API_KEY_IDENTITY_TYPE);
        boolean isOAuth2Request = identityType.equals(EricHeaderHelper.OAUTH2_IDENTITY_TYPE);

        if(isApiKeyRequest) {
            return validateAPI(request, response);
        }
        else if(isOAuth2Request) {
            return validateOAuth2(request, response);
        }
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));
        LOGGER.error("Unrecognised identity type", logMap);
        response.setStatus(UNAUTHORIZED.value());
        return false;
    }

    private boolean validateAPI(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));
        if(AuthorisationUtil.hasInternalUserRole(request) && GET.matches(request.getMethod())) {
            LOGGER.info("internal API is permitted to view the resource", logMap);
            return true;
        } else {
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            LOGGER.error("API is not permitted to perform a "+request.getMethod(), logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }
    }

    private boolean validateOAuth2(HttpServletRequest request, HttpServletResponse response) {
        if (!POST.matches(request.getMethod())) {
            final Map<String, String> pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            final String certifiedCopyId = pathVariables.get("id");

            final String identity = EricHeaderHelper.getIdentity(request);
            Optional<CertifiedCopyItem> item = service.getCertifiedCopyItemById(certifiedCopyId);
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(CERTIFIED_COPY_ID_LOG_KEY, certifiedCopyId);
            logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));
            logMap.put(IDENTITY_LOG_KEY, identity);

            if (item.isPresent()) {
                String userId = item.get().getUserId();
                if (userId == null) {
                    logMap.put(STATUS_LOG_KEY,UNAUTHORIZED);
                    LOGGER.error("No user id found on certified-copy item, all certified-copies should have a user id", logMap);
                    response.setStatus(UNAUTHORIZED.value());
                    return false;
                }
                logMap.put(USER_ID_LOG_KEY, userId);
                boolean authUserIsCreatedBy = userId.equals(identity);
                if (authUserIsCreatedBy) {
                    LOGGER.info("User is permitted to view/edit the resource certified-copy with userId", logMap);
                    return true;
                } else {
                   logMap.put(STATUS_LOG_KEY,UNAUTHORIZED);
                    LOGGER.error("User is not permitted to view/edit the resource certified-copy with userId", logMap);
                    response.setStatus(UNAUTHORIZED.value());
                    return false;
                }
            } else {
                logMap.put(STATUS_LOG_KEY,NOT_FOUND);
                LOGGER.error("Resource certified-copy item not found", logMap);
                response.setStatus(NOT_FOUND.value());
                return false;
            }
        }
        return true;
    }

}
