package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType;

import java.util.ArrayList;
import java.util.List;

@Service
public class CertifiedCopyCostCalculatorService {
    private static final String POSTAGE_COST = "0";
    private static final String DISCOUNT = "0";
    private static final String FILING_HISTORY_TYPE_NEWINC = "NEWINC";

    private final CostsConfig costs;

    public CertifiedCopyCostCalculatorService(CostsConfig costs) {
        this.costs = costs;
    }

    public ItemCostCalculation calculateCosts(final DeliveryTimescale deliveryTimescale,
                                              final String filingHistoryType) {
        checkArguments(deliveryTimescale, filingHistoryType);
        final List<ItemCosts> itemCostsList = new ArrayList<>();
        ItemCosts itemCosts =  calculateSingleItemCosts(deliveryTimescale, filingHistoryType);
        itemCostsList.add(itemCosts);
        final String totalItemCost = calculateTotalItemCost(itemCostsList, POSTAGE_COST);

        return new ItemCostCalculation(itemCostsList, POSTAGE_COST, totalItemCost);
    }

    private ItemCosts calculateSingleItemCosts(final DeliveryTimescale deliveryTimescale,
                                               final String filingHistoryType) {
        final ItemCosts itemCosts = new ItemCosts();
        itemCosts.setDiscountApplied(DISCOUNT);
        final int cost = filingHistoryType.equals(FILING_HISTORY_TYPE_NEWINC) ?
                                            deliveryTimescale.getCertifiedCopyNewIncorporationCost(costs) :
                                            deliveryTimescale.getCertifiedCopyCost(costs);
        itemCosts.setItemCost(Integer.toString(cost));
        itemCosts.setCalculatedCost(Integer.toString(cost));
        final ProductType productType = deliveryTimescale.getProductType();
        itemCosts.setProductType(productType);

        return itemCosts;
    }

    private String calculateTotalItemCost(final List<ItemCosts> costs, final String postageCost) {
        final int total = costs.stream()
                .map(itemCosts -> Integer.parseInt(itemCosts.getCalculatedCost()))
                .reduce(0, Integer::sum) + Integer.parseInt(postageCost);

        return Integer.toString(total);
    }

    private void checkArguments(final DeliveryTimescale deliveryTimescale,
                                String filingHistoryType) {
        if (deliveryTimescale == null) {
            throw new IllegalArgumentException("deliveryTimescale must not be null!");
        }
        if (filingHistoryType == null) {
            throw new IllegalArgumentException("filingHistoryType must not be null");
        }
    }
}
