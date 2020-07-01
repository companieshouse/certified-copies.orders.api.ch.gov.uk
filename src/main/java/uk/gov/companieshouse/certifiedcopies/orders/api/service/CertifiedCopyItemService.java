package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CertifiedCopyItemService {

    private final CertifiedCopyItemRepository repository;
    private final EtagGeneratorService etagGenerator;
    private final LinksGeneratorService linksGenerator;
    private final IdGeneratorService idGenerator;
    private final DescriptionProviderService descriptionProvider;

    public static final String DESCRIPTION_IDENTIFIER = "certified-copy";
    public static final String KIND = "item#certified-copy";
    private static final String COMPANY_NUMBER_KEY = "company_number";

    public CertifiedCopyItemService(final CertifiedCopyItemRepository repository,
                                    final EtagGeneratorService etagGenerator,
                                    final LinksGeneratorService linksGenerator,
                                    final IdGeneratorService idGenerator,
                                    final DescriptionProviderService descriptionProvider) {
        this.repository = repository;
        this.etagGenerator = etagGenerator;
        this.linksGenerator = linksGenerator;
        this.idGenerator = idGenerator;
        this.descriptionProvider = descriptionProvider;
    }

    public CertifiedCopyItem createCertifiedCopyItem(final CertifiedCopyItem certifiedCopyItem) {
        final LocalDateTime now = LocalDateTime.now();
        certifiedCopyItem.setCreatedAt(now);
        certifiedCopyItem.setUpdatedAt(now);
        certifiedCopyItem.setId(idGenerator.autoGenerateId());
        certifiedCopyItem.setEtag(etagGenerator.generateEtag());
        certifiedCopyItem.setLinks(linksGenerator.generateLinks(certifiedCopyItem.getId()));

        String description = descriptionProvider.getDescription(certifiedCopyItem.getData().getCompanyNumber());
        certifiedCopyItem.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        certifiedCopyItem.setDescription(description);
        certifiedCopyItem.setKind(KIND);

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put(DESCRIPTION_IDENTIFIER, description);
        descriptionValues.put(COMPANY_NUMBER_KEY, certifiedCopyItem.getData().getCompanyNumber());
        certifiedCopyItem.setDescriptionValues(descriptionValues);

        if(certifiedCopyItem.getData().getItemOptions().getDeliveryMethod().equals(DeliveryMethod.POSTAL)) {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.TRUE);
        } else {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.FALSE);
        }

        return repository.save(certifiedCopyItem);
    }

    /**
     * Gets the certified copy item by its ID, and returns it as-is, without decorating it in any way.
     * @param id the ID of the certified copy item to be retrieved
     * @return the undecorated item retrieved from the DB
     */
    public Optional<CertifiedCopyItem> getCertifiedCopyItemById(String id) {
        return repository.findById(id);
    }
}
