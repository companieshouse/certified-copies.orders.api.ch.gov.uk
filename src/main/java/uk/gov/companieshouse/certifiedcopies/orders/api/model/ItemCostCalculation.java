package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import java.util.List;

/**
 * An instance of this represents the outcome of a certified copy cost calculation.
 */
public class ItemCostCalculation {

    private final List<ItemCosts> itemCosts;
    private final String postageCost;
    private final String totalItemCost;

    public ItemCostCalculation(List<ItemCosts> itemCosts, String postageCost, String totalItemCost) {
        this.itemCosts = itemCosts;
        this.postageCost = postageCost;
        this.totalItemCost = totalItemCost;
    }

    public List<ItemCosts> getItemCosts() {
        return itemCosts;
    }

    public String getPostageCost() {
        return postageCost;
    }

    public String getTotalItemCost() {
        return totalItemCost;
    }
}
