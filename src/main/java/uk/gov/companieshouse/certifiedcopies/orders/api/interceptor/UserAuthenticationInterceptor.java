package uk.gov.companieshouse.certifiedcopies.orders.api.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.EricHeaderHelper;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.getLogger;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_LOG_KEY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.STATUS_LOG_KEY;

@Component
public class UserAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = getLogger();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));
        final String identityType = EricHeaderHelper.getIdentityType(request);
        if(identityType == null) {
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no authorised identity type", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }

        final String identity = EricHeaderHelper.getIdentity(request);
        if(identity == null) {
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no authorised identity", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;
        }
        return true;
    }
}
