package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItem;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.CertifiedCopyItemData;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryMethod;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.repository.CertifiedCopyItemRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CertifiedCopyItemService {

    private final CertifiedCopyItemRepository repository;
    private final EtagGeneratorService etagGenerator;
    private final LinksGeneratorService linksGenerator;
    private final IdGeneratorService idGenerator;
    private final CertifiedCopyCostCalculatorService costCalculatorService;
    private final DescriptionProviderService descriptionProvider;

    public static final String DESCRIPTION_IDENTIFIER = "certified-copy";
    public static final String KIND = "item#certified-copy";
    private static final String COMPANY_NUMBER_KEY = "company_number";

    public CertifiedCopyItemService(final CertifiedCopyItemRepository repository,
                                    final EtagGeneratorService etagGenerator,
                                    final LinksGeneratorService linksGenerator,
                                    final IdGeneratorService idGenerator,
                                    final CertifiedCopyCostCalculatorService calculatorService,
                                    final DescriptionProviderService descriptionProvider) {
        this.repository = repository;
        this.etagGenerator = etagGenerator;
        this.linksGenerator = linksGenerator;
        this.idGenerator = idGenerator;
        this.costCalculatorService = calculatorService;
        this.descriptionProvider = descriptionProvider;
    }

    private void populateDescriptions(final CertifiedCopyItem certifiedCopyItem) {
        String description = descriptionProvider.getDescription(certifiedCopyItem.getData().getCompanyNumber());
        certifiedCopyItem.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        certifiedCopyItem.setDescription(description);

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put(DESCRIPTION_IDENTIFIER, description);
        descriptionValues.put(COMPANY_NUMBER_KEY, certifiedCopyItem.getData().getCompanyNumber());
        certifiedCopyItem.setDescriptionValues(descriptionValues);
    }

    public CertifiedCopyItem createCertifiedCopyItem(final CertifiedCopyItem certifiedCopyItem) {
        final LocalDateTime now = LocalDateTime.now();
        certifiedCopyItem.setCreatedAt(now);
        certifiedCopyItem.setUpdatedAt(now);
        certifiedCopyItem.setId(idGenerator.autoGenerateId());
        certifiedCopyItem.setEtag(etagGenerator.generateEtag());
        certifiedCopyItem.setLinks(linksGenerator.generateLinks(certifiedCopyItem.getId()));
        certifiedCopyItem.setKind(KIND);

        populateDescriptions(certifiedCopyItem);

        if(certifiedCopyItem.getData().getItemOptions().getDeliveryMethod().equals(DeliveryMethod.POSTAL)) {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.TRUE);
        } else {
            certifiedCopyItem.getData().setPostalDelivery(Boolean.FALSE);
        }
        certifiedCopyItem.getData().setKind(KIND);
        populateItemCosts(certifiedCopyItem, costCalculatorService);

        return repository.save(certifiedCopyItem);
    }

    /**
     * Saves the certificate item, assumed to have been updated, to the database.
     *
     * @param updatedCertifiedCopyItem the certificate item to save
     * @return the latest certified copy item state resulting from the save
     */
    public CertifiedCopyItem saveCertifiedCopyItem(final CertifiedCopyItem updatedCertifiedCopyItem) {
        final LocalDateTime now = LocalDateTime.now();
        updatedCertifiedCopyItem.setUpdatedAt(now);
        populateDescriptions(updatedCertifiedCopyItem);
        updatedCertifiedCopyItem.setEtag(etagGenerator.generateEtag());
        final CertifiedCopyItem itemSaved = repository.save(updatedCertifiedCopyItem);
        populateItemCosts(itemSaved, costCalculatorService);
        return itemSaved;
    }

    public void populateItemCosts(final CertifiedCopyItem item, final CertifiedCopyCostCalculatorService calculator) {
        CertifiedCopyItemData itemData = item.getData();
        List<FilingHistoryDocument> filingHistoryDocumentList = itemData.getItemOptions().getFilingHistoryDocuments();
        int quantity = itemData.getQuantity();
        List<ItemCostCalculation> costCalculationList = calculator.calculateAllCosts(itemData.getQuantity(),
                                                                            getOrDefaultDeliveryTimescale(item),
                                                                            filingHistoryDocumentList);
        int totalItemCost = 0;
        List<ItemCosts> itemCosts = new ArrayList<>();
        for (ItemCostCalculation costCalculation : costCalculationList) {
            totalItemCost += Integer.parseInt(costCalculation.getTotalItemCost());
            itemCosts.addAll(costCalculation.getItemCosts());
            item.setPostageCost(costCalculation.getPostageCost());
        }
        item.setItemCosts(itemCosts);
        item.setTotalItemCost(totalItemCost + "");
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
