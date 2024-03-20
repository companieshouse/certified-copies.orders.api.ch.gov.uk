package uk.gov.companieshouse.certifiedcopies.orders.api.interceptor;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.logging.util.RequestLogger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor, RequestLogger {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logStartRequestProcessing(request, LoggingUtils.getLogger());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           @Nullable ModelAndView modelAndView) {

        logEndRequestProcessing(request, response, LoggingUtils.getLogger());
    }

}
