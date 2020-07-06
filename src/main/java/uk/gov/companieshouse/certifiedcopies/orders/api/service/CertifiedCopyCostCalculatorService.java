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

    private final CostsConfig costs;

    public CertifiedCopyCostCalculatorService(CostsConfig costs) {
        this.costs = costs;
    }

    public ItemCostCalculation calculateCosts(final int quantity, final DeliveryTimescale deliveryTimescale) {
        checkArguments(quantity, deliveryTimescale);
        final List<ItemCosts> itemCostsList = new ArrayList<>();
        for (int itemNumber = 1; itemNumber <= quantity; itemNumber++) {
            ItemCosts itemCosts =  calculateSingleItemCosts(itemNumber, deliveryTimescale);
            itemCostsList.add(itemCosts);
        }
        final String totalItemCost = calculateTotalItemCost(itemCostsList, POSTAGE_COST);
        return new ItemCostCalculation(itemCostsList, POSTAGE_COST, totalItemCost);
    }

    private ItemCosts calculateSingleItemCosts(final int itemNumber, final DeliveryTimescale deliveryTimescale) {
        final ItemCosts cost = new ItemCosts();
        final int discountApplied = 0;
        cost.setDiscountApplied(Integer.toString(discountApplied));
        cost.setItemCost(Integer.toString(deliveryTimescale.getCertifiedCopyCost(costs)));
        final int calculatedCost = deliveryTimescale.getCertifiedCopyCost(costs) - discountApplied;
        cost.setCalculatedCost(Integer.toString(calculatedCost));
        final ProductType productType = deliveryTimescale.getProductType();
        cost.setProductType(productType);
        return cost;

    }

    private String calculateTotalItemCost(final List<ItemCosts> costs, final String postageCost) {
        final int total = costs.stream()
                .map(itemCosts -> Integer.parseInt(itemCosts.getCalculatedCost()))
                .reduce(0, Integer::sum) + Integer.parseInt(postageCost);
        return Integer.toString(total);
    }

    private void checkArguments(final int quantity, final DeliveryTimescale deliveryTimescale) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 1!");
        }
        if (deliveryTimescale == null) {
            throw new IllegalArgumentException("deliveryTimescale must not be null!");
        }
    }
}
