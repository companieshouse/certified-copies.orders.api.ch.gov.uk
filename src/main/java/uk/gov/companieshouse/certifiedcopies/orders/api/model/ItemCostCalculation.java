package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import java.util.List;

/**
 * An instance of this represents the outcome of a certified copy cost calculation.
 */
public record ItemCostCalculation(List<ItemCosts> itemCosts, String postageCost, String totalItemCost) {

}
