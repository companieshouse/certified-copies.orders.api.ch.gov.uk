package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CertifiedCopyItemService {

    private final CertifiedCopyItemRepository repository;
    private final EtagGeneratorService etagGenerator;
    private final LinksGeneratorService linksGenerator;
    private final IdGeneratorService idGenerator;

    public CertifiedCopyItemService(final CertifiedCopyItemRepository repository,
                                    final EtagGeneratorService etagGenerator,
                                    final LinksGeneratorService linksGenerator,
                                    final IdGeneratorService idGenerator) {
        this.repository = repository;
        this.etagGenerator = etagGenerator;
        this.linksGenerator = linksGenerator;
        this.idGenerator = idGenerator;
    }

    public CertifiedCopyItem createCertifiedCopyItem(final CertifiedCopyItem certifiedCopyItem) {
        final LocalDateTime now = LocalDateTime.now();
        certifiedCopyItem.setCreatedAt(now);
        certifiedCopyItem.setUpdatedAt(now);
        certifiedCopyItem.setId(idGenerator.autoGenerateId());
        certifiedCopyItem.setEtag(etagGenerator.generateEtag());
        certifiedCopyItem.setLinks(linksGenerator.generateLinks(certifiedCopyItem.getId()));

        if(certifiedCopyItem.getData().getItemOptions().getDeliveryMethod().equals(DeliveryMethod.POSTAL)) {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.TRUE);
        } else {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.FALSE);
        }
        certifiedCopyItem.getData().setKind("item#certified-copy");

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
