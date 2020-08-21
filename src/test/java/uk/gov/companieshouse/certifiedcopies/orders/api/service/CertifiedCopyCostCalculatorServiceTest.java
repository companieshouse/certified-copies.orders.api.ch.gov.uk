package uk.gov.companieshouse.certifiedcopies.orders.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCostCalculation;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ItemCosts;
import uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType;
import uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit/integration tests the {@link CertifiedCopyCostCalculatorService} class.
 */
@SpringBootTest
class CertifiedCopyCostCalculatorServiceTest {

    private static final String POSTAGE_COST = "0";
    private static final String NO_DISCOUNT = "0";
    private static final String FILING_HISTORY_ID_01 = "01";
    private static final String FILING_HISTORY_ID_02 = "02";
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
    private static final int QUANTITY = 1;
    private static final int INVALID_QUANTITY = 0;

    @Autowired
    private CertifiedCopyCostCalculatorService calculatorUnderTest;

    @Test
    @DisplayName("Calculates standard delivery certified copy cost correctly")
    void calculatesCertifiedCopyCostCorrectly() {

        // Given and when
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocuments(FILING_HISTORY_TYPE_CH01);
        final List<ItemCostCalculation> calculations =
                calculatorUnderTest.calculateAllCosts(QUANTITY, DeliveryTimescale.STANDARD, filingHistoryDocumentList);
        final List<ItemCosts> costs = calculations.get(0).getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(CERTIFIED_COPY_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(CERTIFIED_COPY_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY));
        assertThat(calculations.get(0).getPostageCost(), is(POSTAGE_COST));
        assertThat(calculations.get(0).getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));

        final FilingHistoryDocument filingHistoryDocument = filingHistoryDocumentList.get(0);
        assertThat(filingHistoryDocument.getFilingHistoryCost(), is(CERTIFIED_COPY_COST));
    }

    @Test
    @DisplayName("Calculates standard delivery certified copy cost correctly")
    void calculatesCertifiedCopiesCostCorrectly() {

        // Given and when
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocumentsMulti();
        final List<ItemCostCalculation> calculations =
                calculatorUnderTest.calculateAllCosts(QUANTITY, DeliveryTimescale.STANDARD, filingHistoryDocumentList);
        final List<ItemCosts> costs = calculations.get(0).getItemCosts();

        // Then
        final ItemCosts cost0 = calculations.get(0).getItemCosts().get(0);
        assertThat(cost0.getItemCost(), is(CERTIFIED_COPY_COST));
        assertThat(cost0.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost0.getCalculatedCost(), is(CERTIFIED_COPY_COST));
        assertThat(cost0.getProductType(), is(ProductType.CERTIFIED_COPY));
        assertThat(calculations.get(0).getPostageCost(), is(POSTAGE_COST));
        assertThat(calculations.get(0).getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));
        final ItemCosts cost1 = calculations.get(1).getItemCosts().get(0);
        assertThat(cost1.getItemCost(), is(CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost1.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost1.getCalculatedCost(), is(CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost1.getProductType(), is(ProductType.CERTIFIED_COPY_INCORPORATION));
        assertThat(calculations.get(0).getPostageCost(), is(POSTAGE_COST));
        assertThat(calculations.get(0).getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));

        final FilingHistoryDocument filingHistoryDocument1 = filingHistoryDocumentList.get(0);
        assertThat(filingHistoryDocument1.getFilingHistoryCost(), is(CERTIFIED_COPY_COST));

        final FilingHistoryDocument filingHistoryDocument2 = filingHistoryDocumentList.get(1);
        assertThat(filingHistoryDocument2.getFilingHistoryCost(), is(CERTIFIED_COPY_NEW_INC_COST));
    }

    @Test
    @DisplayName("Calculates standard delivery certified copy new incorporation cost correctly")
    void calculatesCertifiedCopyNewIncorporationCostCorrectly() {

        // Given and when
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocuments(FILING_HISTORY_TYPE_NEWINC);
        final List<ItemCostCalculation> calculations =
                calculatorUnderTest.calculateAllCosts(QUANTITY, DeliveryTimescale.STANDARD, filingHistoryDocumentList);
        final List<ItemCosts> costs = calculations.get(0).getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY_INCORPORATION));
        assertThat(calculations.get(0).getPostageCost(), is(POSTAGE_COST));
        assertThat(calculations.get(0).getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));

        final FilingHistoryDocument filingHistoryDocument = filingHistoryDocumentList.get(0);
        assertThat(filingHistoryDocument.getFilingHistoryCost(), is(CERTIFIED_COPY_NEW_INC_COST));
    }

    @Test
    @DisplayName("Calculates same day delivery certified copy cost correctly")
    void calculatesCertifiedCopySameDayCostCorrectly() {

        // Given and when
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocuments(FILING_HISTORY_TYPE_CH01);
        final List<ItemCostCalculation> calculations =
                calculatorUnderTest.calculateAllCosts(QUANTITY, DeliveryTimescale.SAME_DAY, filingHistoryDocumentList);
        final List<ItemCosts> costs = calculations.get(0).getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY_SAME_DAY));
        assertThat(calculations.get(0).getPostageCost(), is(POSTAGE_COST));
        assertThat(calculations.get(0).getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));

        final FilingHistoryDocument filingHistoryDocument = filingHistoryDocumentList.get(0);
        assertThat(filingHistoryDocument.getFilingHistoryCost(), is(SAME_DAY_CERTIFIED_COPY_COST));
    }

    @Test
    @DisplayName("Calculates same day delivery certified copy new incorporation cost correctly")
    void calculatesCertifiedCopyNewIncorporationSameDayCostCorrectly() {

        // Given and when
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocuments(FILING_HISTORY_TYPE_NEWINC);
        final List<ItemCostCalculation> calculations =
                calculatorUnderTest.calculateAllCosts(QUANTITY, DeliveryTimescale.SAME_DAY, filingHistoryDocumentList);
        final List<ItemCosts> costs = calculations.get(0).getItemCosts();

        // Then
        final ItemCosts cost = costs.get(0);
        assertThat(cost.getItemCost(), is(SAME_DAY_CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getDiscountApplied(), is(NO_DISCOUNT));
        assertThat(cost.getCalculatedCost(), is(SAME_DAY_CERTIFIED_COPY_NEW_INC_COST));
        assertThat(cost.getProductType(), is(ProductType.CERTIFIED_COPY_INCORPORATION_SAME_DAY));
        assertThat(calculations.get(0).getPostageCost(), is(POSTAGE_COST));
        assertThat(calculations.get(0).getTotalItemCost(), is(calculateExpectedTotalItemCost(costs, POSTAGE_COST)));

        final FilingHistoryDocument filingHistoryDocument = filingHistoryDocumentList.get(0);
        assertThat(filingHistoryDocument.getFilingHistoryCost(), is(SAME_DAY_CERTIFIED_COPY_NEW_INC_COST));
    }

    @Test
    @DisplayName("Incorrect quantity results in IllegalArgumentException")
    void incorrectQuantityTriggersIllegalArgumentException() {
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocuments(FILING_HISTORY_TYPE_CH01);
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> calculatorUnderTest.calculateAllCosts(INVALID_QUANTITY, DeliveryTimescale.STANDARD,
                                filingHistoryDocumentList));
        assertThat(exception.getMessage(), is("quantity must be greater than or equal to 1!"));
    }

    @Test
    @DisplayName("Missing filing history type results in IllegalArgumentException")
    void missingFilingHistoryTypeTriggersIllegalArgumentException() {
        FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
        filingHistoryDocument.setFilingHistoryId(FILING_HISTORY_ID_01);
        List<FilingHistoryDocument> filingHistoryDocumentList = new ArrayList<>();
        filingHistoryDocumentList.add(filingHistoryDocument);
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> calculatorUnderTest.calculateAllCosts(QUANTITY, DeliveryTimescale.STANDARD,
                                                                                    filingHistoryDocumentList));
        assertThat(exception.getMessage(), is("filingHistoryType must not be null"));
    }

    @Test
    @DisplayName("null delivery timescale results in an IllegalArgumentException")
    void noDeliveryTimescaleTriggersIllegalArgumentException() {
        List<FilingHistoryDocument> filingHistoryDocumentList = getFilingHistoryDocuments(FILING_HISTORY_TYPE_CH01);
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> calculatorUnderTest.calculateAllCosts(QUANTITY, null, filingHistoryDocumentList));
        assertThat(exception.getMessage(), is("deliveryTimescale must not be null!"));
    }

    private List<FilingHistoryDocument> getFilingHistoryDocuments(String filingHistoryType) {
        FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
        filingHistoryDocument.setFilingHistoryId(FILING_HISTORY_ID_01);
        filingHistoryDocument.setFilingHistoryType(filingHistoryType);
        List<FilingHistoryDocument> filingHistoryDocumentList = new ArrayList<>();
        filingHistoryDocumentList.add(filingHistoryDocument);

        return filingHistoryDocumentList;
    }

    private List<FilingHistoryDocument> getFilingHistoryDocumentsMulti() {
        FilingHistoryDocument filingHistoryDocument1 = new FilingHistoryDocument();
        filingHistoryDocument1.setFilingHistoryId(FILING_HISTORY_ID_01);
        filingHistoryDocument1.setFilingHistoryType(FILING_HISTORY_TYPE_CH01);
        FilingHistoryDocument filingHistoryDocument2 = new FilingHistoryDocument();
        filingHistoryDocument2.setFilingHistoryId(FILING_HISTORY_ID_02);
        filingHistoryDocument2.setFilingHistoryType(FILING_HISTORY_TYPE_NEWINC);
        List<FilingHistoryDocument> filingHistoryDocumentList = new ArrayList<>();
        filingHistoryDocumentList.add(filingHistoryDocument1);
        filingHistoryDocumentList.add(filingHistoryDocument2);

        return filingHistoryDocumentList;
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
