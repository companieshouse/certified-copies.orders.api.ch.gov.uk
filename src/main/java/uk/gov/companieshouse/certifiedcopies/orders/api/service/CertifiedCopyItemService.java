package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CertifiedCopyItemService {

    private final CertifiedCopyItemRepository repository;
    private final EtagGeneratorService etagGenerator;
    private final LinksGeneratorService linksGenerator;
    private final IdGeneratorService idGenerator;
    private final CertifiedCopyCostCalculatorService costCalculatorService;

    public CertifiedCopyItemService(final CertifiedCopyItemRepository repository,
                                    final EtagGeneratorService etagGenerator,
                                    final LinksGeneratorService linksGenerator,
                                    final IdGeneratorService idGenerator,
                                    final CertifiedCopyCostCalculatorService calculatorService) {
        this.repository = repository;
        this.etagGenerator = etagGenerator;
        this.linksGenerator = linksGenerator;
        this.idGenerator = idGenerator;
        this.costCalculatorService = calculatorService;
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
        populateItemCosts(certifiedCopyItem, costCalculatorService);

        return repository.save(certifiedCopyItem);
    }

    public void populateItemCosts(final CertifiedCopyItem item, final CertifiedCopyCostCalculatorService calculator) {
        CertifiedCopyItemData itemData = item.getData();
        String filingHistoryType = itemData.getItemOptions().getFilingHistoryDocuments().get(0).getFilingHistoryType();

        final ItemCostCalculation calculation =
                calculator.calculateCosts(getOrDefaultDeliveryTimescale(item), filingHistoryType);
        item.setPostageCost(calculation.getPostageCost());
        item.setItemCosts(calculation.getItemCosts());
        item.setTotalItemCost(calculation.getTotalItemCost());
    }

    DeliveryTimescale getOrDefaultDeliveryTimescale(final CertifiedCopyItem item) {
        return item.getData().getItemOptions() != null &&
                item.getData().getItemOptions().getDeliveryTimescale() != null ?
                item.getData().getItemOptions().getDeliveryTimescale() :
                DeliveryTimescale.STANDARD;
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
