package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale.SAME_DAY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.DeliveryTimescale.STANDARD;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_INCORPORATION;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_INCORPORATION_SAME_DAY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.model.ProductType.CERTIFIED_COPY_SAME_DAY;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_NEW_INCORPORATION_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.FILING_HISTORY_TYPE_CH01;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.FILING_HISTORY_TYPE_NEWINC;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.SAME_DAY_CERTIFIED_COPY_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST;

/**
 * Unit/integration tests the {@link DeliveryTimescale} enum.
 */
class DeliveryTimescaleTest {

    private static final CostsConfig COSTS;

    static {
        COSTS = new CostsConfig();
        COSTS.setStandardCost(CERTIFIED_COPY_COST);
        COSTS.setSameDayCost(SAME_DAY_CERTIFIED_COPY_COST);
        COSTS.setStandardNewIncorporationCost(CERTIFIED_COPY_NEW_INCORPORATION_COST);
        COSTS.setSameDayNewIncorporationCost(SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST);
    }

   @Test
   void serialisesCorrectlyToJson() throws JsonProcessingException {
       final ObjectMapper mapper = new ObjectMapper();
       final String standardJson = mapper.writeValueAsString(STANDARD);
       final String sameDayJson = mapper.writeValueAsString(SAME_DAY);

       assertThat(standardJson, is("\"standard\""));
       assertThat(sameDayJson, is("\"same-day\""));
   }

   @Test
    void standardTimescaleCostsAreCorrect() {
       assertThat(STANDARD.getCost(COSTS, FILING_HISTORY_TYPE_CH01), is(CERTIFIED_COPY_COST));
       assertThat(STANDARD.getCost(COSTS, FILING_HISTORY_TYPE_NEWINC), is(CERTIFIED_COPY_NEW_INCORPORATION_COST));
       assertThat(STANDARD.getProductType(FILING_HISTORY_TYPE_CH01), is(CERTIFIED_COPY));
       assertThat(STANDARD.getProductType(FILING_HISTORY_TYPE_NEWINC), is(CERTIFIED_COPY_INCORPORATION));
   }

    @Test
    void sameDayTimescaleCostsAreCorrect() {
        assertThat(SAME_DAY.getCost(COSTS, FILING_HISTORY_TYPE_CH01), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(SAME_DAY.getCost(COSTS, FILING_HISTORY_TYPE_NEWINC), is(SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST));
        assertThat(SAME_DAY.getProductType(FILING_HISTORY_TYPE_CH01), is(CERTIFIED_COPY_SAME_DAY));
        assertThat(SAME_DAY.getProductType(FILING_HISTORY_TYPE_NEWINC), is(CERTIFIED_COPY_INCORPORATION_SAME_DAY));
    }
}
