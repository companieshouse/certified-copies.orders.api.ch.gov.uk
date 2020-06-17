package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.Links;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Service that generates the links for the certified copy id specified.
 */
@Service
public class LinksGeneratorService {

    private final String pathToSelf;

    /**
     * Constructor.
     * @param pathToSelf configured path to self URI
     */
    public LinksGeneratorService(
            final @Value("${uk.gov.companieshouse.certifiedcopies.orders.api.home}") String pathToSelf) {
        if (isBlank(pathToSelf)) {
            throw new IllegalArgumentException("Path to self URI not configured!");
        }
        this.pathToSelf = pathToSelf;
    }

    /**
     * Generates the links for the certified copy item identified.
     * @param certifiedCopyId the ID for the certifiedCopy
     * @return the appropriate {@link Links}
     */
    public Links generateLinks(final String certifiedCopyId) {
        if (isBlank(certifiedCopyId)) {
            throw new IllegalArgumentException("Certified Copy ID not populated!");
        }
        final Links links = new Links();
        links.setSelf(pathToSelf + "/" + certifiedCopyId);
        return links;
    }

}
