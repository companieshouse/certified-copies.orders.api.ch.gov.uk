package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.certifiedcopies.orders.api.logging.LoggingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Service
public class DescriptionProviderService {

    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String CERTIFIED_COPY_DESCRIPTION_KEY = "certified-copy-description";
    private static final String COMPANY_CERTIFIED_COPY_DESCRIPTION_KEY = "company-certified-copy";

    private static final String ORDERS_DESCRIPTIONS_FILEPATH = "api-enumerations/orders_descriptions.yaml";

    private final String companyCertifiedCopyDescription;

    private static final String LOG_MESSAGE_FILE_KEY = "file";

    public DescriptionProviderService() {
        final File ordersDescriptionsFile = new File(ORDERS_DESCRIPTIONS_FILEPATH);
        companyCertifiedCopyDescription = getCompanyCertifiedCopyDescription(ordersDescriptionsFile);
    }

    public DescriptionProviderService(final File ordersDescriptionsFile) {
        companyCertifiedCopyDescription = getCompanyCertifiedCopyDescription(ordersDescriptionsFile);
    }

    /**
     * Gets the configured description.
     * @param companyNumber the company number making up part of the description
     * @return the configured description, or <code>null</code> if none found.
     */
    public String getDescription(final String companyNumber) {
        if (companyCertifiedCopyDescription == null) {
            // Error logged again here at time description is requested.
            LoggingUtils.logOrdersDescriptionsConfigError(COMPANY_CERTIFIED_COPY_DESCRIPTION_KEY,
                    "Company certified copy description not found in orders descriptions file");
            return null;
        }
        final Map<String, String> descriptionValues = singletonMap(COMPANY_NUMBER_KEY, companyNumber);
        return StrSubstitutor.replace(companyCertifiedCopyDescription, descriptionValues, "{", "}");
    }

    /**
     * Looks up the company certified copy description by its key 'company-certified-copy' under the
     * 'certified-copy-description' section of the orders descriptions YAML file.
     * @param ordersDescriptionsFile the orders descriptions YAML file
     * @return the value found or <code>null</code> if none found.
     */
    private String getCompanyCertifiedCopyDescription(final File ordersDescriptionsFile) {

        if (!ordersDescriptionsFile.exists()) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_MESSAGE_FILE_KEY, ordersDescriptionsFile.getAbsolutePath());
            LoggingUtils.getLogger().error("Orders descriptions file not found", logMap);
            return null;
        }

        String companyCertifiedCopyDesc = null;
        try(final InputStream inputStream = new FileInputStream(ordersDescriptionsFile)) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> orderDescriptions = yaml.load(inputStream);
            final Map<String, String> certifiedCopyDescriptions =
                    (Map<String, String>) orderDescriptions.get(CERTIFIED_COPY_DESCRIPTION_KEY);
            if (certifiedCopyDescriptions == null) {
                LoggingUtils.logOrdersDescriptionsConfigError(CERTIFIED_COPY_DESCRIPTION_KEY,
                        "Certified copy descriptions not found in orders descriptions file");
                return null;
            }

            companyCertifiedCopyDesc = certifiedCopyDescriptions.get(COMPANY_CERTIFIED_COPY_DESCRIPTION_KEY);
            if (companyCertifiedCopyDesc == null) {
                LoggingUtils.logOrdersDescriptionsConfigError(COMPANY_CERTIFIED_COPY_DESCRIPTION_KEY,
                        "Company certified copy description not found in orders descriptions file");
            }
        } catch (IOException ioe) {
            // This is very unlikely to happen here given File.exists() check above,
            // and that it is not likely to encounter an error closing the stream either.
        }
        return companyCertifiedCopyDesc;
    }

}
