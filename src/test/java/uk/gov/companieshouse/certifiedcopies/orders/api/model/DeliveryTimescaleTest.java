package uk.gov.companieshouse.certifiedcopies.orders.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.certifiedcopies.orders.api.config.CostsConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.certifiedcopies.orders.api.util.TestConstants.*;

/**
 * Unit/integration tests the {@link DeliveryTimescale} enum.
 */
class DeliveryTimescaleTest {

    private static final CostsConfig COSTS;

    static {
        COSTS = new CostsConfig();
        COSTS.setStandardCost(CERTIFIED_COPY_COST);
        COSTS.setSameDayCost(SAME_DAY_CERTIFIED_COPY_COST);
        COSTS.setStandardDiscount(CERTIFIED_COPY_NEW_INCORPORATION_COST);
        COSTS.setSameDayDiscount(SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST);
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
       assertThat(DeliveryTimescale.STANDARD.getIndividualCertificateCost(COSTS),         is(CERTIFIED_COPY_COST));
       assertThat(DeliveryTimescale.STANDARD.getExtraCertificateDiscount(COSTS),          is(CERTIFIED_COPY_NEW_INCORPORATION_COST));
       MatcherAssert.assertThat(DeliveryTimescale.STANDARD.getFirstCertificateProductType(),            Is.is(ProductType.CERTIFICATE));
       MatcherAssert.assertThat(DeliveryTimescale.STANDARD.getAdditionalCertificatesProductType(),      Is.is(ProductType.CERTIFICATE_ADDITIONAL_COPY));
   }

    @Test
    void sameDayTimescaleCostsAreCorrect() {
        assertThat(DeliveryTimescale.SAME_DAY.getIndividualCertificateCost(COSTS),         is(SAME_DAY_CERTIFIED_COPY_COST));
        assertThat(DeliveryTimescale.SAME_DAY.getExtraCertificateDiscount(COSTS),          is(SAME_DAY_CERTIFIED_COPY_NEW_INCORPORATION_COST));
        MatcherAssert.assertThat(DeliveryTimescale.SAME_DAY.getFirstCertificateProductType(),            Is.is(ProductType.CERTIFICATE_SAME_DAY));
        MatcherAssert.assertThat(DeliveryTimescale.SAME_DAY.getAdditionalCertificatesProductType(),      Is.is(ProductType.CERTIFICATE_ADDITIONAL_COPY));
    }

}
