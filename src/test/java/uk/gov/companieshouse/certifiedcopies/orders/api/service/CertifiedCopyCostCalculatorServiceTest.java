package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit/integration tests the {@link CertifiedCopyCostCalculatorService} class.
 */
@SpringBootTest
public class CertifiedCopyCostCalculatorServiceTest {

    private static final String POSTAGE_COST = "0";
    private static final String NO_DISCOUNT = "0";
    private static final String FILING_HISTORY_TYPE_CH01 = "CH01";
    private static final String FILING_HISTORY_TYPE_NEWINC = "NEWINC";
    private static final String CERTIFIED_COPY_COST =
            Integer.toString(TestConstants.CERTIFIED_COPY_COST);
    private static final String CERTIFIED_COPY_NEW_INC_COST =
            Integer.toString(TestConstants.CERTIFIED_COPY_NEW_INCORPORATION_COST);
    private static final String SAME_DAY_CERTIFIED_COPY_COST =
            Integer.toString(TestConstants.SAME_DAY_CERTIFIED_COPY_COST);
    private static final String SAME_DAY_CERTIFIED_COPY_NEW_INC_COST =
            Integer.toString(TestConstants.SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST);

    @Autowired
    private CertifiedCopyCostCalculatorService calculatorUnderTest;

    @Test
    @DisplayName("Calculates standard delivery certified copy cost correctly")
    void calculatesCertifiedCopyCostCorrectly() {

        // Given and when
        final ItemCostCalculation calculation =
                calculatorUnderTest.calculateCosts(DeliveryTimescale.STANDARD, FILING_HISTORY_TYPE_CH01);
        final List<ItemCosts> costs = calculation.getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(CERTIFIED_COPY_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(CERTIFIED_COPY_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY));
        assertThat(calculation.getPostageCost(), is(POSTAGE_COST));
        assertThat(calculation.getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));
    }

    @Test
    @DisplayName("Calculates standard delivery certified copy new incorporation cost correctly")
    void calculatesCertifiedCopyNewIncorporationCostCorrectly() {

        // Given and when
        final ItemCostCalculation calculation =
                calculatorUnderTest.calculateCosts(DeliveryTimescale.STANDARD, FILING_HISTORY_TYPE_NEWINC);
        final List<ItemCosts> costs = calculation.getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY));
        assertThat(calculation.getPostageCost(), is(POSTAGE_COST));
        assertThat(calculation.getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));
    }

    @Test
    @DisplayName("Calculates same day delivery certified copy cost correctly")
    void calculatesCertifiedCopySameDayCostCorrectly() {

        // Given and when
        final ItemCostCalculation calculation =
                calculatorUnderTest.calculateCosts(DeliveryTimescale.SAME_DAY, FILING_HISTORY_TYPE_CH01);
        final List<ItemCosts> costs = calculation.getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY_SAME_DAY));
        assertThat(calculation.getPostageCost(), is(POSTAGE_COST));
        assertThat(calculation.getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));
    }

    @Test
    @DisplayName("Calculates same day delivery certified copy new incorporation cost correctly")
    void calculatesCertifiedCopyNewIncorporationSameDayCostCorrectly() {

        // Given and when
        final ItemCostCalculation calculation =
                calculatorUnderTest.calculateCosts(DeliveryTimescale.SAME_DAY, FILING_HISTORY_TYPE_NEWINC);
        final List<ItemCosts> costs = calculation.getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(SAME_DAY_CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(SAME_DAY_CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY_SAME_DAY));
        assertThat(calculation.getPostageCost(), is(POSTAGE_COST));
        assertThat(calculation.getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));
    }

    @Test
    @DisplayName("Missing filing history type results in IllegalArgumentException")
    void missingFilingHistoryTypeTriggersIllegalArgumentException() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> calculatorUnderTest.calculateCosts(DeliveryTimescale.STANDARD, null));
        assertThat(exception.getMessage(), is("filingHistoryType must not be null"));
    }

    @Test
    @DisplayName("null delivery timescale results in an IllegalArgumentException")
    void noDeliveryTimescaleTriggersIllegalArgumentException() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> calculatorUnderTest.calculateCosts(null, FILING_HISTORY_TYPE_NEWINC));
        assertThat(exception.getMessage(), is("deliveryTimescale must not be null!"));
    }

    /**
     * Utility that calculates the expected total item cost for the item costs and postage cost provided.
     * @param costs the item costs
     * @param postageCost the postage cost
     * @return the expected total item cost (as a String)
     */
    private String calculateExpectedTotalItemCost(final List<ItemCosts> costs, final String postageCost) {
        final Integer total = costs.stream()
                               .map(itemCosts -> Integer.parseInt(itemCosts.getCalculatedCost()))
                               .reduce(0, Integer::sum) + Integer.parseInt(postageCost);
        return total.toString();
    }

}
