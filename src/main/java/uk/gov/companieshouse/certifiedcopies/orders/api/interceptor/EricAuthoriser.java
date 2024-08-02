package uk.gov.companieshouse.certifiedcopies.orders.api.interceptor;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.StringHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

import static java.util.Objects.isNull;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.EricHeaderHelper.ERIC_AUTHORISED_ROLES;
import static uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

@Component
public
class EricAuthoriser {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final StringHelper stringHelper;

    public EricAuthoriser(final StringHelper stringHelper) {
        this.stringHelper = stringHelper;
    }

    public boolean hasPermission(final String permission, final HttpServletRequest request) {
        final String authorisedRolesHeader = request.getHeader(ERIC_AUTHORISED_ROLES);

        LOGGER.debug("Checking " + ERIC_AUTHORISED_ROLES + " header with value `"
                + authorisedRolesHeader + "` for permission `" + permission + "`.");

        if (isNull(authorisedRolesHeader)) {
            return false;
        }

        final Set<String> permissions = stringHelper.asSet("\\s+", authorisedRolesHeader);
        return permissions.contains(permission);
    }
}
