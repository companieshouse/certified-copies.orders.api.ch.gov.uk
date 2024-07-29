package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType;

import java.util.ArrayList;
import java.util.List;

@Service
public class CertifiedCopyCostCalculatorService {
    private static final String POSTAGE_COST = "0";
    // private static final String DISCOUNT = "0";

    private final CostsConfig costs;

    public CertifiedCopyCostCalculatorService(CostsConfig costs) {
        this.costs = costs;
    }

    public List<ItemCostCalculation> calculateAllCosts(final int quantity, final DeliveryTimescale deliveryTimescale,
                                                       final List<FilingHistoryDocument> filingHistoryDocs) {
        List<ItemCostCalculation> costCalculationList = new ArrayList<>();

        for (FilingHistoryDocument filingHistoryDocument : filingHistoryDocs) {
            ItemCostCalculation calculatedCost = calculateCosts(quantity, deliveryTimescale,
                    filingHistoryDocument.getFilingHistoryType(), false);

            filingHistoryDocument.setFilingHistoryCost(calculatedCost.itemCosts().getFirst().getCalculatedCost());

            costCalculationList.add(calculatedCost);
        }

        return costCalculationList;
    }

    private ItemCostCalculation calculateCosts(final int quantity,  final DeliveryTimescale deliveryTimescale,
                                               final String filingHistoryType, boolean userGetsFreeCertificates) {
        checkArguments(quantity, deliveryTimescale, filingHistoryType);
        final List<ItemCosts> itemCostsList = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            itemCostsList.add(calculateSingleItemCosts(deliveryTimescale, filingHistoryType, userGetsFreeCertificates));
        }
        final String totalItemCost = calculateTotalItemCost(itemCostsList, POSTAGE_COST);

        return new ItemCostCalculation(itemCostsList, POSTAGE_COST, totalItemCost);
    }

    private ItemCosts calculateSingleItemCosts(final DeliveryTimescale deliveryTimescale,
                                               final String filingHistoryType, boolean userGetsFreeCertificates) {
        final ItemCosts itemCosts = new ItemCosts();
        final int cost = deliveryTimescale.getCost(costs, filingHistoryType);
        final int discountApplied = userGetsFreeCertificates ? cost : 0;
        itemCosts.setDiscountApplied(Integer.toString(discountApplied));
        itemCosts.setItemCost(Integer.toString(cost));
        final int calculatedCost = cost - discountApplied;
        
        itemCosts.setCalculatedCost(Integer.toString(calculatedCost));

        final ProductType productType = deliveryTimescale.getProductType(filingHistoryType);
        itemCosts.setProductType(productType);

        return itemCosts;
    }

    private String calculateTotalItemCost(final List<ItemCosts> costs, final String postageCost) {
        final int total = costs.stream()
                .map(itemCosts -> Integer.parseInt(itemCosts.getCalculatedCost()))
                .reduce(0, Integer::sum) + Integer.parseInt(postageCost);

        return Integer.toString(total);
    }

    private void checkArguments(final int quantity,
                                final DeliveryTimescale deliveryTimescale,
                                String filingHistoryType) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 1!");
        }
        if (deliveryTimescale == null) {
            throw new IllegalArgumentException("deliveryTimescale must not be null!");
        }
        if (filingHistoryType == null) {
            throw new IllegalArgumentException("filingHistoryType must not be null");
        }
    }
}
