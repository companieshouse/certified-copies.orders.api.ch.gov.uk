package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_COST;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.CERTIFIED_COPY_NEW_INCORPORATION_COST;
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
       final String standardJson = mapper.writeValueAsString(DeliveryTimescale.STANDARD);
       final String sameDayJson = mapper.writeValueAsString(DeliveryTimescale.SAME_DAY);

       assertThat(standardJson, is("\"standard\""));
       assertThat(sameDayJson, is("\"same-day\""));
   }

   @Test
    void standardTimescaleCostsAreCorrect() {
       assertThat(DeliveryTimescale.STANDARD.getCertifiedCopyCost(COSTS), is(CERTIFIED_COPY_COST));
       assertThat(DeliveryTimescale.STANDARD.getCertifiedCopyNewIncorporationCost(COSTS),
                                            is(CERTIFIED_COPY_NEW_INCORPORATION_COST));
       MatcherAssert.assertThat(DeliveryTimescale.STANDARD.getProductType(), Is.is(ProductType.CERTIFIED_COPY));
   }

    @Test
    void sameDayTimescaleCostsAreCorrect() {
        assertThat(DeliveryTimescale.SAME_DAY.getCertifiedCopyCost(COSTS), is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(DeliveryTimescale.SAME_DAY.getCertifiedCopyNewIncorporationCost(COSTS),
                                            is(SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST));
        MatcherAssert.assertThat(DeliveryTimescale.SAME_DAY.getProductType(),
                                            Is.is(ProductType.CERTIFIED_COPY_SAME_DAY));
    }
}
